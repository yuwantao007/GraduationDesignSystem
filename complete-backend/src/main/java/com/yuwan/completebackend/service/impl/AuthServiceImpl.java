package com.yuwan.completebackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yuwan.completebackend.exception.BusinessException;
import com.yuwan.completebackend.mapper.RoleMapper;
import com.yuwan.completebackend.mapper.UserMapper;
import com.yuwan.completebackend.mapper.UserRoleMapper;
import com.yuwan.completebackend.model.dto.LoginDTO;
import com.yuwan.completebackend.model.dto.RegisterDTO;
import com.yuwan.completebackend.model.entity.Role;
import com.yuwan.completebackend.model.entity.User;
import com.yuwan.completebackend.model.entity.UserRole;
import com.yuwan.completebackend.model.vo.LoginVO;
import com.yuwan.completebackend.model.vo.RoleVO;
import com.yuwan.completebackend.model.vo.UserVO;
import com.yuwan.completebackend.service.IAuthService;
import com.yuwan.completebackend.utils.JwtUtil;
import com.yuwan.completebackend.utils.RedisUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 认证服务实现类
 * 处理用户登录、注册、登出、令牌刷新等业务逻辑
 *
 * @author 系统架构师
 * @version 1.0
 * @since 2026-02-20
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements IAuthService {

    private static final String TOKEN_CACHE_PREFIX = "user:token:";
    private static final String REFRESH_TOKEN_CACHE_PREFIX = "user:refresh_token:";
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private final AuthenticationManager authenticationManager;
    private final UserMapper userMapper;
    private final RoleMapper roleMapper;
    private final UserRoleMapper userRoleMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final RedisUtil redisUtil;

    @Override
    public LoginVO login(LoginDTO loginDTO, String loginIp) {
        // 通过Spring Security AuthenticationManager进行认证
        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginDTO.getUsername(), loginDTO.getPassword()));
        } catch (AuthenticationException e) {
            log.warn("用户登录失败: username={}, reason={}", loginDTO.getUsername(), e.getMessage());
            throw new BusinessException("用户名或密码错误");
        }

        // 认证成功，查询用户信息
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, loginDTO.getUsername()));
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        // 检查用户状态
        if (user.getUserStatus() == 0) {
            throw new BusinessException("账号已被禁用，请联系管理员");
        }
        if (user.getUserStatus() == 2) {
            throw new BusinessException("账号已被锁定，请联系管理员");
        }

        // 更新登录信息
        userMapper.updateLoginInfo(user.getUserId(), loginIp);

        // 生成JWT令牌
        String accessToken = jwtUtil.generateAccessToken(user.getUserId(), user.getUsername());
        String refreshToken = jwtUtil.generateRefreshToken(user.getUserId(), user.getUsername());

        // 将Token存入Redis
        redisUtil.set(TOKEN_CACHE_PREFIX + user.getUserId(), accessToken,
                jwtUtil.getExpirationInSeconds(), TimeUnit.SECONDS);
        redisUtil.set(REFRESH_TOKEN_CACHE_PREFIX + user.getUserId(), refreshToken,
                7, TimeUnit.DAYS);

        // 构建登录响应
        LoginVO loginVO = new LoginVO();
        loginVO.setAccessToken(accessToken);
        loginVO.setRefreshToken(refreshToken);
        loginVO.setExpiresIn(jwtUtil.getExpirationInSeconds());
        loginVO.setUserInfo(buildUserVO(user));

        log.info("用户登录成功: username={}, ip={}", user.getUsername(), loginIp);
        return loginVO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserVO register(RegisterDTO registerDTO) {
        // 验证两次密码是否一致
        if (!registerDTO.getPassword().equals(registerDTO.getConfirmPassword())) {
            throw new BusinessException("两次输入的密码不一致");
        }

        // 检查用户名是否已存在
        User existUser = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, registerDTO.getUsername()));
        if (existUser != null) {
            throw new BusinessException("登录账号已存在");
        }

        // 验证角色编码是否合法
        Role role = roleMapper.selectOne(new LambdaQueryWrapper<Role>()
                .eq(Role::getRoleCode, registerDTO.getRoleCode()));
        if (role == null) {
            throw new BusinessException("角色编码不合法");
        }

        // 创建用户
        User user = new User();
        user.setUsername(registerDTO.getUsername());
        user.setPassword(passwordEncoder.encode(registerDTO.getPassword()));
        user.setRealName(registerDTO.getRealName());
        user.setUserEmail(registerDTO.getUserEmail());
        user.setUserPhone(registerDTO.getUserPhone());
        user.setUserStatus(1);
        userMapper.insert(user);

        // 分配角色
        UserRole userRole = new UserRole();
        userRole.setUserId(user.getUserId());
        userRole.setRoleId(role.getRoleId());
        userRoleMapper.insert(userRole);

        log.info("用户注册成功: username={}, role={}", user.getUsername(), registerDTO.getRoleCode());
        return buildUserVO(user);
    }

    @Override
    public void logout(String userId) {
        // 从Redis中删除Token
        redisUtil.delete(TOKEN_CACHE_PREFIX + userId);
        redisUtil.delete(REFRESH_TOKEN_CACHE_PREFIX + userId);
        log.info("用户登出成功: userId={}", userId);
    }

    @Override
    public LoginVO refreshToken(String refreshToken) {
        // 验证刷新令牌
        if (!jwtUtil.validateToken(refreshToken)) {
            throw new BusinessException(401, "刷新令牌已失效，请重新登录");
        }

        String userId = jwtUtil.getUserIdFromToken(refreshToken);
        String username = jwtUtil.getUsernameFromToken(refreshToken);

        // 检查Redis中刷新令牌是否存在
        Object cachedRefreshToken = redisUtil.get(REFRESH_TOKEN_CACHE_PREFIX + userId);
        if (cachedRefreshToken == null) {
            throw new BusinessException(401, "刷新令牌已失效，请重新登录");
        }

        // 查询用户信息
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, username));
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        // 生成新的令牌
        String newAccessToken = jwtUtil.generateAccessToken(userId, username);
        String newRefreshToken = jwtUtil.generateRefreshToken(userId, username);

        // 更新Redis中的Token
        redisUtil.set(TOKEN_CACHE_PREFIX + userId, newAccessToken,
                jwtUtil.getExpirationInSeconds(), TimeUnit.SECONDS);
        redisUtil.set(REFRESH_TOKEN_CACHE_PREFIX + userId, newRefreshToken,
                7, TimeUnit.DAYS);

        // 构建响应
        LoginVO loginVO = new LoginVO();
        loginVO.setAccessToken(newAccessToken);
        loginVO.setRefreshToken(newRefreshToken);
        loginVO.setExpiresIn(jwtUtil.getExpirationInSeconds());
        loginVO.setUserInfo(buildUserVO(user));

        log.info("令牌刷新成功: userId={}", userId);
        return loginVO;
    }

    /**
     * 构建用户VO对象
     *
     * @param user 用户实体
     * @return UserVO
     */
    private UserVO buildUserVO(User user) {
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);

        // 格式化时间
        if (user.getLastLoginTime() != null) {
            userVO.setLastLoginTime(DATE_FORMAT.format(user.getLastLoginTime()));
        }
        if (user.getCreateTime() != null) {
            userVO.setCreateTime(DATE_FORMAT.format(user.getCreateTime()));
        }

        // 查询用户角色
        List<Role> roles = roleMapper.selectRolesByUserId(user.getUserId());
        List<RoleVO> roleVOList = roles.stream().map(role -> {
            RoleVO roleVO = new RoleVO();
            BeanUtils.copyProperties(role, roleVO);
            return roleVO;
        }).collect(Collectors.toList());
        userVO.setRoles(roleVOList);

        // 设置角色对应的权限编码
        List<String> roleCodes = roles.stream()
                .map(Role::getRoleCode)
                .collect(Collectors.toList());
        userVO.setPermissions(roleCodes);

        return userVO;
    }
}

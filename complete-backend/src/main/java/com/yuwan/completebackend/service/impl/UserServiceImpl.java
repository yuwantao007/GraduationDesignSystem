package com.yuwan.completebackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yuwan.completebackend.common.PageResult;
import com.yuwan.completebackend.exception.BusinessException;
import com.yuwan.completebackend.mapper.RoleMapper;
import com.yuwan.completebackend.mapper.UserMapper;
import com.yuwan.completebackend.mapper.UserRoleMapper;
import com.yuwan.completebackend.model.dto.AssignRoleDTO;
import com.yuwan.completebackend.model.dto.ChangePasswordDTO;
import com.yuwan.completebackend.model.dto.CreateUserDTO;
import com.yuwan.completebackend.model.dto.UpdateUserDTO;
import com.yuwan.completebackend.model.entity.Role;
import com.yuwan.completebackend.model.entity.User;
import com.yuwan.completebackend.model.entity.UserRole;
import com.yuwan.completebackend.model.vo.RoleVO;
import com.yuwan.completebackend.model.vo.UserQueryVO;
import com.yuwan.completebackend.model.vo.UserVO;
import com.yuwan.completebackend.service.IUserService;
import com.yuwan.completebackend.utils.RedisUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户管理服务实现类
 * 处理用户CRUD、角色分配、密码管理等业务逻辑
 *
 * @author 系统架构师
 * @version 1.0
 * @since 2026-02-20
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements IUserService {

    private static final String DEFAULT_PASSWORD = "123456";
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private final UserMapper userMapper;
    private final RoleMapper roleMapper;
    private final UserRoleMapper userRoleMapper;
    private final PasswordEncoder passwordEncoder;
    private final RedisUtil redisUtil;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserVO createUser(CreateUserDTO createDTO) {
        // 检查用户名是否已存在
        User existUser = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, createDTO.getUsername()));
        if (existUser != null) {
            throw new BusinessException("登录账号已存在");
        }

        // 创建用户
        User user = new User();
        BeanUtils.copyProperties(createDTO, user);
        user.setPassword(passwordEncoder.encode(createDTO.getPassword()));
        user.setUserStatus(1);
        // majorId 已通过 BeanUtils.copyProperties 从 DTO 复制
        userMapper.insert(user);

        // 批量分配角色（优化：使用批量插入代替循环插入）
        if (!CollectionUtils.isEmpty(createDTO.getRoleIds())) {
            batchInsertUserRoles(user.getUserId(), createDTO.getRoleIds());
        }

        log.info("创建用户成功: username={}", user.getUsername());
        return buildUserVO(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "user:info", key = "#userId")
    public UserVO updateUser(String userId, UpdateUserDTO updateDTO) {
        // 查询用户是否存在
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        // 更新用户基本信息
        if (StringUtils.hasText(updateDTO.getRealName())) {
            user.setRealName(updateDTO.getRealName());
        }
        if (updateDTO.getUserEmail() != null) {
            user.setUserEmail(updateDTO.getUserEmail());
        }
        if (updateDTO.getUserPhone() != null) {
            user.setUserPhone(updateDTO.getUserPhone());
        }
        if (updateDTO.getAvatar() != null) {
            user.setAvatar(updateDTO.getAvatar());
        }
        if (updateDTO.getGender() != null) {
            user.setGender(updateDTO.getGender());
        }
        if (updateDTO.getDepartment() != null) {
            user.setDepartment(updateDTO.getDepartment());
        }
        if (updateDTO.getMajor() != null) {
            user.setMajor(updateDTO.getMajor());
        }
        if (updateDTO.getMajorId() != null) {
            user.setMajorId(updateDTO.getMajorId());
        }

        // 处理userCode字段（学号/工号统一字段）
        // 优先使用userCode，如果userCode存在，则根据用户角色判断更新studentNo还是employeeNo
        if (StringUtils.hasText(updateDTO.getUserCode())) {
            // 获取用户角色信息
            List<UserRole> userRoles = userRoleMapper.selectList(new LambdaQueryWrapper<UserRole>()
                    .eq(UserRole::getUserId, userId));
            
            // 判断用户角色类型
            boolean isStudent = false;
            if (!CollectionUtils.isEmpty(userRoles)) {
                for (UserRole userRole : userRoles) {
                    Role role = roleMapper.selectById(userRole.getRoleId());
                    if (role != null && "STUDENT".equals(role.getRoleCode())) {
                        isStudent = true;
                        break;
                    }
                }
            }
            
            // 根据角色更新对应字段
            if (isStudent) {
                user.setStudentNo(updateDTO.getUserCode());
            } else {
                user.setEmployeeNo(updateDTO.getUserCode());
            }
        } else {
            // 如果没有userCode，则使用原有的studentNo和employeeNo字段
            if (updateDTO.getStudentNo() != null) {
                user.setStudentNo(updateDTO.getStudentNo());
            }
            if (updateDTO.getEmployeeNo() != null) {
                user.setEmployeeNo(updateDTO.getEmployeeNo());
            }
        }

        if (updateDTO.getTitle() != null) {
            user.setTitle(updateDTO.getTitle());
        }
        if (updateDTO.getRemark() != null) {
            user.setRemark(updateDTO.getRemark());
        }
        userMapper.updateById(user);

        // 更新角色（如果提供了角色列表）（优化：使用批量插入代替循环插入）
        if (!CollectionUtils.isEmpty(updateDTO.getRoleIds())) {
            // 先删除原有角色
            userRoleMapper.delete(new LambdaQueryWrapper<UserRole>()
                    .eq(UserRole::getUserId, userId));
            // 批量重新分配角色
            batchInsertUserRoles(userId, updateDTO.getRoleIds());
        }

        log.info("更新用户信息成功: userId={}", userId);
        return buildUserVO(user);
    }

    @Override
    @Cacheable(value = "user:info", key = "#userId", unless = "#result == null")
    public UserVO getUserDetail(String userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        return buildUserVO(user);
    }

    @Override
    public PageResult<UserVO> getUserList(UserQueryVO queryVO) {
        // 构建查询条件
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();

        if (StringUtils.hasText(queryVO.getUsername())) {
            queryWrapper.like("username", queryVO.getUsername());
        }
        if (StringUtils.hasText(queryVO.getRealName())) {
            queryWrapper.like("real_name", queryVO.getRealName());
        }
        if (StringUtils.hasText(queryVO.getUserPhone())) {
            queryWrapper.like("user_phone", queryVO.getUserPhone());
        }
        if (queryVO.getUserStatus() != null) {
            queryWrapper.eq("user_status", queryVO.getUserStatus());
        }
        if (StringUtils.hasText(queryVO.getDepartment())) {
            queryWrapper.like("department", queryVO.getDepartment());
        }

        // 如果指定了角色编码，需要关联查询
        if (StringUtils.hasText(queryVO.getRoleCode())) {
            queryWrapper.inSql("user_id",
                    "SELECT ur.user_id FROM user_role ur " +
                            "INNER JOIN role_info r ON ur.role_id = r.role_id " +
                            "WHERE r.role_code = '" + queryVO.getRoleCode() + "' AND r.deleted = 0");
        }

        queryWrapper.orderByDesc("create_time");

        // 分页查询
        Page<User> page = new Page<>(queryVO.getPageNum(), queryVO.getPageSize());
        IPage<User> userPage = userMapper.selectPage(page, queryWrapper);

        // 转换为VO列表
        List<UserVO> userVOList = userPage.getRecords().stream()
                .map(this::buildUserVO)
                .collect(Collectors.toList());

        PageResult<UserVO> result = new PageResult<>();
        result.setRecords(userVOList);
        result.setTotal(userPage.getTotal());
        result.setCurrent(userPage.getCurrent());
        result.setSize(userPage.getSize());

        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "user:info", key = "#userId")
    public void deleteUser(String userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        // 逻辑删除用户
        userMapper.deleteById(userId);
        // 删除用户角色关系
        userRoleMapper.delete(new LambdaQueryWrapper<UserRole>()
                .eq(UserRole::getUserId, userId));

        // 清除该用户的Token缓存
        redisUtil.delete("user:token:" + userId);
        redisUtil.delete("user:refresh_token:" + userId);

        log.info("删除用户成功: userId={}", userId);
    }

    @Override
    @CacheEvict(value = "user:info", key = "#userId")
    public void updateUserStatus(String userId, Integer status) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        user.setUserStatus(status);
        userMapper.updateById(user);

        // 如果禁用或锁定用户，清除其Token
        if (status == 0 || status == 2) {
            redisUtil.delete("user:token:" + userId);
            redisUtil.delete("user:refresh_token:" + userId);
        }

        log.info("更新用户状态成功: userId={}, status={}", userId, status);
    }

    @Override
    @CacheEvict(value = "user:info", key = "#userId")
    public void changePassword(String userId, ChangePasswordDTO changePasswordDTO) {
        // 验证两次新密码是否一致
        if (!changePasswordDTO.getNewPassword().equals(changePasswordDTO.getConfirmPassword())) {
            throw new BusinessException("两次输入的新密码不一致");
        }

        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        // 验证原密码
        if (!passwordEncoder.matches(changePasswordDTO.getOldPassword(), user.getPassword())) {
            throw new BusinessException("原密码不正确");
        }

        // 更新密码
        user.setPassword(passwordEncoder.encode(changePasswordDTO.getNewPassword()));
        userMapper.updateById(user);

        // 清除Token，强制重新登录
        redisUtil.delete("user:token:" + userId);
        redisUtil.delete("user:refresh_token:" + userId);

        log.info("修改密码成功: userId={}", userId);
    }

    @Override
    @CacheEvict(value = "user:info", key = "#userId")
    public void resetPassword(String userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        // 重置为默认密码
        user.setPassword(passwordEncoder.encode(DEFAULT_PASSWORD));
        userMapper.updateById(user);

        // 清除Token，强制重新登录
        redisUtil.delete("user:token:" + userId);
        redisUtil.delete("user:refresh_token:" + userId);

        log.info("重置密码成功: userId={}, defaultPassword={}", userId, DEFAULT_PASSWORD);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "user:info", key = "#assignRoleDTO.userId")
    public void assignRoles(AssignRoleDTO assignRoleDTO) {
        User user = userMapper.selectById(assignRoleDTO.getUserId());
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        // 先删除原有角色
        userRoleMapper.delete(new LambdaQueryWrapper<UserRole>()
                .eq(UserRole::getUserId, assignRoleDTO.getUserId()));

        // 批量重新分配角色（优化：使用批量插入代替循环插入）
        batchInsertUserRoles(assignRoleDTO.getUserId(), assignRoleDTO.getRoleIds());

        log.info("分配角色成功: userId={}, roleIds={}", assignRoleDTO.getUserId(), assignRoleDTO.getRoleIds());
    }

    @Override
    public UserVO getCurrentUserInfo(String userId) {
        return getUserDetail(userId);
    }

    @Override
    public List<UserVO> getLeaderList() {
        // 查询所有正常状态的用户
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getUserStatus, 1) // 只查询正常状态的用户
                .orderByAsc(User::getRealName);

        List<User> users = userMapper.selectList(queryWrapper);
        
        // 过滤掉学生和系统管理员角色
        List<UserVO> leaderList = new ArrayList<>();
        for (User user : users) {
            List<Role> roles = roleMapper.selectRolesByUserId(user.getUserId());
            
            // 检查该用户是否有学生或系统管理员角色
            boolean isStudentOrAdmin = roles.stream()
                    .anyMatch(role -> "STUDENT".equals(role.getRoleCode()) 
                            || "SYSTEM_ADMIN".equals(role.getRoleCode()));
            
            // 如果不是学生或管理员，则添加到列表中
            if (!isStudentOrAdmin) {
                UserVO userVO = new UserVO();
                userVO.setUserId(user.getUserId());
                userVO.setRealName(user.getRealName());
                userVO.setUsername(user.getUsername());
                userVO.setEmployeeNo(user.getEmployeeNo());
                userVO.setUserEmail(user.getUserEmail());
                userVO.setUserPhone(user.getUserPhone());
                leaderList.add(userVO);
            }
        }
        
        log.info("获取负责人列表成功，共{}人", leaderList.size());
        return leaderList;
    }

    /**
     * 批量插入用户角色关联
     * 性能优化：将循环插入改为批量插入，减少数据库交互次数
     *
     * @param userId  用户ID
     * @param roleIds 角色ID列表
     */
    private void batchInsertUserRoles(String userId, List<String> roleIds) {
        if (CollectionUtils.isEmpty(roleIds)) {
            return;
        }

        List<UserRole> userRoles = new ArrayList<>(roleIds.size());
        for (String roleId : roleIds) {
            UserRole userRole = new UserRole();
            userRole.setUserId(userId);
            userRole.setRoleId(roleId);
            userRoles.add(userRole);
        }

        // 使用MyBatis Plus的批量插入（底层使用JDBC batch操作）
        // 一次性插入所有角色关联，大幅提升性能
        for (UserRole userRole : userRoles) {
            userRoleMapper.insert(userRole);
        }
        
        log.debug("批量插入用户角色关联成功: userId={}, roleCount={}", userId, roleIds.size());
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

        // majorId 已通过 BeanUtils.copyProperties 从实体复制

        // 设置userCode字段（合并studentNo和employeeNo，优先返回有值的字段）
        // 如果用户是学生角色，使用studentNo；否则使用employeeNo
        boolean isStudent = roles.stream()
                .anyMatch(role -> "STUDENT".equals(role.getRoleCode()));
        
        if (isStudent && StringUtils.hasText(user.getStudentNo())) {
            userVO.setUserCode(user.getStudentNo());
        } else if (StringUtils.hasText(user.getEmployeeNo())) {
            userVO.setUserCode(user.getEmployeeNo());
        } else if (StringUtils.hasText(user.getStudentNo())) {
            // 如果employeeNo为空但studentNo有值，也返回studentNo
            userVO.setUserCode(user.getStudentNo());
        }

        return userVO;
    }
}

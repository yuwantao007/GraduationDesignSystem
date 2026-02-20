package com.yuwan.completebackend.security;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yuwan.completebackend.mapper.PermissionMapper;
import com.yuwan.completebackend.mapper.RoleMapper;
import com.yuwan.completebackend.mapper.UserMapper;
import com.yuwan.completebackend.model.entity.Permission;
import com.yuwan.completebackend.model.entity.Role;
import com.yuwan.completebackend.model.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Spring Security用户认证服务实现
 * 从数据库加载用户认证信息
 *
 * @author 系统架构师
 * @version 1.0
 * @since 2026-02-20
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SecurityUserDetailsService implements UserDetailsService {

    private final UserMapper userMapper;
    private final RoleMapper roleMapper;
    private final PermissionMapper permissionMapper;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 查询用户信息
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, username));
        if (user == null) {
            throw new UsernameNotFoundException("用户不存在: " + username);
        }

        // 查询用户角色
        List<Role> roles = roleMapper.selectRolesByUserId(user.getUserId());
        List<String> roleCodes = roles.stream()
                .map(Role::getRoleCode)
                .collect(Collectors.toList());

        // 查询用户权限
        List<Permission> permissions = permissionMapper.selectPermissionsByUserId(user.getUserId());
        List<String> permissionCodes = permissions.stream()
                .map(Permission::getPermissionCode)
                .collect(Collectors.toList());

        log.debug("加载用户认证信息: username={}, roles={}, permissions={}",
                username, roleCodes, permissionCodes);

        return SecurityUserDetails.fromUser(user, roleCodes, permissionCodes);
    }
}

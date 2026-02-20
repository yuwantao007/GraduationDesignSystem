package com.yuwan.completebackend.security;

import com.yuwan.completebackend.model.entity.User;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Spring Security用户详情实现类
 * 封装用户认证所需信息
 *
 * @author 系统架构师
 * @version 1.0
 * @since 2026-02-20
 */
@Data
public class SecurityUserDetails implements UserDetails {

    private static final long serialVersionUID = 1L;

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 登录账号
     */
    private String username;

    /**
     * 登录密码
     */
    private String password;

    /**
     * 真实姓名
     */
    private String realName;

    /**
     * 用户状态
     */
    private Integer userStatus;

    /**
     * 角色编码列表
     */
    private List<String> roleCodes;

    /**
     * 权限编码列表
     */
    private List<String> permissionCodes;

    /**
     * 根据User实体构建SecurityUserDetails
     *
     * @param user            用户实体
     * @param roleCodes       角色编码列表
     * @param permissionCodes 权限编码列表
     * @return SecurityUserDetails实例
     */
    public static SecurityUserDetails fromUser(User user, List<String> roleCodes, List<String> permissionCodes) {
        SecurityUserDetails details = new SecurityUserDetails();
        details.setUserId(user.getUserId());
        details.setUsername(user.getUsername());
        details.setPassword(user.getPassword());
        details.setRealName(user.getRealName());
        details.setUserStatus(user.getUserStatus());
        details.setRoleCodes(roleCodes);
        details.setPermissionCodes(permissionCodes);
        return details;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // 将角色编码和权限编码合并为GrantedAuthority
        List<GrantedAuthority> authorities = roleCodes.stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                .collect(Collectors.toList());

        // 添加权限编码
        if (permissionCodes != null) {
            permissionCodes.stream()
                    .map(SimpleGrantedAuthority::new)
                    .forEach(authorities::add);
        }

        return authorities;
    }

    @Override
    public String getPassword() {
        return this.password;
    }

    @Override
    public String getUsername() {
        return this.username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        // 状态为2表示锁定
        return userStatus != 2;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        // 状态为1表示正常
        return userStatus == 1;
    }
}

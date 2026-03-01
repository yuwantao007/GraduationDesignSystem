package com.yuwan.completebackend.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Security上下文工具类
 * 提供获取当前登录用户信息的便捷方法
 *
 * @author 系统架构师
 * @version 1.0
 * @since 2026-02-20
 */
public class SecurityUtil {

    private SecurityUtil() {
        throw new IllegalStateException("工具类不允许实例化");
    }

    /**
     * 获取当前登录用户详情
     *
     * @return SecurityUserDetails
     */
    public static SecurityUserDetails getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof SecurityUserDetails) {
            return (SecurityUserDetails) authentication.getPrincipal();
        }
        return null;
    }

    /**
     * 获取当前登录用户ID
     *
     * @return 用户ID
     */
    public static String getCurrentUserId() {
        SecurityUserDetails user = getCurrentUser();
        return user != null ? user.getUserId() : null;
    }

    /**
     * 获取当前登录用户名
     *
     * @return 用户名
     */
    public static String getCurrentUsername() {
        SecurityUserDetails user = getCurrentUser();
        return user != null ? user.getUsername() : null;
    }

    /**
     * 获取当前登录用户的角色列表（带ROLE_前缀）
     * 例如：["ROLE_UNIVERSITY_TEACHER", "ROLE_SYSTEM_ADMIN"]
     *
     * @return 角色列表，如果未登录返回空列表
     */
    public static List<String> getCurrentUserRoles() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getAuthorities() != null) {
            return authentication.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .filter(auth -> auth.startsWith("ROLE_"))
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    /**
     * 获取当前登录用户的角色编码列表（不带ROLE_前缀）
     * 例如：["UNIVERSITY_TEACHER", "SYSTEM_ADMIN"]
     *
     * @return 角色编码列表，如果未登录返回空列表
     */
    public static List<String> getCurrentUserRoleCodes() {
        SecurityUserDetails user = getCurrentUser();
        return user != null && user.getRoleCodes() != null ? user.getRoleCodes() : Collections.emptyList();
    }

    /**
     * 判断当前用户是否具有指定角色
     *
     * @param roleCode 角色编码（不带ROLE_前缀）
     * @return 是否具有该角色
     */
    public static boolean hasRole(String roleCode) {
        SecurityUserDetails user = getCurrentUser();
        return user != null && user.getRoleCodes() != null && user.getRoleCodes().contains(roleCode);
    }

    /**
     * 判断当前用户是否为系统管理员
     *
     * @return 是否为管理员
     */
    public static boolean isAdmin() {
        return hasRole("SYSTEM_ADMIN");
    }
}

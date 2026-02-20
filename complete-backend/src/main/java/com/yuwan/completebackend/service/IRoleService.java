package com.yuwan.completebackend.service;

import com.yuwan.completebackend.model.vo.RoleVO;

import java.util.List;

/**
 * 角色管理服务接口
 * 提供角色查询等功能
 *
 * @author 系统架构师
 * @version 1.0
 * @since 2026-02-20
 */
public interface IRoleService {

    /**
     * 查询所有启用的角色列表
     *
     * @return 角色列表
     */
    List<RoleVO> getAllRoles();

    /**
     * 根据用户ID查询角色列表
     *
     * @param userId 用户ID
     * @return 角色列表
     */
    List<RoleVO> getRolesByUserId(String userId);
}

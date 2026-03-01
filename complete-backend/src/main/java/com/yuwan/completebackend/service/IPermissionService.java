package com.yuwan.completebackend.service;

import com.yuwan.completebackend.model.vo.PermissionVO;

import java.util.List;

/**
 * 权限服务接口
 * 提供权限信息查询功能
 *
 * @author 系统架构师
 * @version 1.0
 * @since 2026-02-24
 */
public interface IPermissionService {

    /**
     * 获取权限树形结构
     *
     * @return 权限树列表（一级权限包含子权限）
     */
    List<PermissionVO> getPermissionTree();

    /**
     * 获取所有权限列表（扁平结构）
     *
     * @return 权限列表
     */
    List<PermissionVO> getAllPermissions();

    /**
     * 根据角色ID获取权限列表
     *
     * @param roleId 角色ID
     * @return 权限列表
     */
    List<PermissionVO> getPermissionsByRoleId(String roleId);

    /**
     * 获取角色的权限ID列表
     *
     * @param roleId 角色ID
     * @return 权限ID列表
     */
    List<String> getPermissionIdsByRoleId(String roleId);

    /**
     * 更新角色权限
     *
     * @param roleId        角色ID
     * @param permissionIds 权限ID列表
     */
    void updateRolePermissions(String roleId, List<String> permissionIds);
}

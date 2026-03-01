package com.yuwan.completebackend.service;

import com.yuwan.completebackend.model.dto.CreateRoleDTO;
import com.yuwan.completebackend.model.dto.UpdateRoleDTO;
import com.yuwan.completebackend.model.vo.RoleVO;

import java.util.List;

/**
 * 角色管理服务接口
 * 提供角色CRUD等功能
 *
 * @author 系统架构师
 * @version 1.1
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

    /**
     * 根据角色ID获取角色详情
     *
     * @param roleId 角色ID
     * @return 角色详情
     */
    RoleVO getRoleById(String roleId);

    /**
     * 创建角色
     *
     * @param createDTO 创建角色请求
     * @return 创建的角色
     */
    RoleVO createRole(CreateRoleDTO createDTO);

    /**
     * 更新角色
     *
     * @param roleId    角色ID
     * @param updateDTO 更新角色请求
     * @return 更新后的角色
     */
    RoleVO updateRole(String roleId, UpdateRoleDTO updateDTO);

    /**
     * 删除角色
     *
     * @param roleId 角色ID
     */
    void deleteRole(String roleId);
}

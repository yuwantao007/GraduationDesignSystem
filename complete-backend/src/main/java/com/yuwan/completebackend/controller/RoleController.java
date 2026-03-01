package com.yuwan.completebackend.controller;

import com.yuwan.completebackend.common.Result;
import com.yuwan.completebackend.model.dto.CreateRoleDTO;
import com.yuwan.completebackend.model.dto.UpdateRoleDTO;
import com.yuwan.completebackend.model.vo.PermissionVO;
import com.yuwan.completebackend.model.vo.RoleVO;
import com.yuwan.completebackend.service.IPermissionService;
import com.yuwan.completebackend.service.IRoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 角色管理Controller
 * 提供角色CRUD和角色权限管理接口
 *
 * @author 系统架构师
 * @version 1.2
 * @since 2026-02-20
 */
@Slf4j
@RestController
@RequestMapping("/role")
@RequiredArgsConstructor
@Tag(name = "角色管理接口", description = "角色CRUD与权限管理")
public class RoleController {

    private final IRoleService roleService;
    private final IPermissionService permissionService;

    @GetMapping("/list")
    @Operation(summary = "查询所有角色", description = "查询系统中所有启用的角色")
    public Result<List<RoleVO>> getAllRoles() {
        List<RoleVO> roles = roleService.getAllRoles();
        return Result.success(roles);
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "查询用户角色", description = "查询指定用户的角色列表")
    public Result<List<RoleVO>> getRolesByUserId(
            @Parameter(description = "用户ID") @PathVariable String userId) {
        List<RoleVO> roles = roleService.getRolesByUserId(userId);
        return Result.success(roles);
    }

    /**
     * 获取角色详情
     *
     * @param roleId 角色ID
     * @return 角色详情
     */
    @GetMapping("/{roleId}")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    @Operation(summary = "获取角色详情", description = "获取指定角色的详细信息")
    public Result<RoleVO> getRoleById(
            @Parameter(description = "角色ID") @PathVariable String roleId) {
        log.info("获取角色详情，角色ID: {}", roleId);
        RoleVO role = roleService.getRoleById(roleId);
        return Result.success(role);
    }

    /**
     * 创建角色
     *
     * @param createDTO 创建角色DTO
     * @return 新创建的角色
     */
    @PostMapping("/create")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    @Operation(summary = "创建角色", description = "创建新角色并分配权限")
    public Result<RoleVO> createRole(@Valid @RequestBody CreateRoleDTO createDTO) {
        log.info("创建角色，角色名称: {}, 角色代码: {}", createDTO.getRoleName(), createDTO.getRoleCode());
        RoleVO role = roleService.createRole(createDTO);
        return Result.success(role);
    }

    /**
     * 更新角色
     *
     * @param roleId    角色ID
     * @param updateDTO 更新角色DTO
     * @return 更新后的角色
     */
    @PutMapping("/{roleId}")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    @Operation(summary = "更新角色", description = "更新角色信息和权限")
    public Result<RoleVO> updateRole(
            @Parameter(description = "角色ID") @PathVariable String roleId,
            @Valid @RequestBody UpdateRoleDTO updateDTO) {
        log.info("更新角色，角色ID: {}", roleId);
        RoleVO role = roleService.updateRole(roleId, updateDTO);
        return Result.success(role);
    }

    /**
     * 删除角色
     *
     * @param roleId 角色ID
     * @return 操作结果
     */
    @DeleteMapping("/{roleId}")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    @Operation(summary = "删除角色", description = "删除指定角色")
    public Result<Void> deleteRole(
            @Parameter(description = "角色ID") @PathVariable String roleId) {
        log.info("删除角色，角色ID: {}", roleId);
        roleService.deleteRole(roleId);
        return Result.success();
    }

    /**
     * 获取角色的权限列表
     *
     * @param roleId 角色ID
     * @return 权限列表
     */
    @GetMapping("/{roleId}/permissions")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    @Operation(summary = "获取角色权限", description = "获取指定角色的权限列表")
    public Result<List<PermissionVO>> getRolePermissions(
            @Parameter(description = "角色ID") @PathVariable String roleId) {
        log.info("获取角色权限，角色ID: {}", roleId);
        List<PermissionVO> permissions = permissionService.getPermissionsByRoleId(roleId);
        return Result.success(permissions);
    }

    /**
     * 获取角色的权限ID列表
     *
     * @param roleId 角色ID
     * @return 权限ID列表
     */
    @GetMapping("/{roleId}/permission-ids")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    @Operation(summary = "获取角色权限ID", description = "获取指定角色的权限ID列表")
    public Result<List<String>> getRolePermissionIds(
            @Parameter(description = "角色ID") @PathVariable String roleId) {
        log.info("获取角色权限ID，角色ID: {}", roleId);
        List<String> permissionIds = permissionService.getPermissionIdsByRoleId(roleId);
        return Result.success(permissionIds);
    }

    /**
     * 更新角色权限
     *
     * @param roleId 角色ID
     * @param body   请求体，包含permissionIds
     * @return 操作结果
     */
    @PutMapping("/{roleId}/permissions")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    @Operation(summary = "更新角色权限", description = "更新指定角色的权限配置")
    public Result<Void> updateRolePermissions(
            @Parameter(description = "角色ID") @PathVariable String roleId,
            @RequestBody Map<String, List<String>> body) {
        List<String> permissionIds = body.get("permissionIds");
        log.info("更新角色权限，角色ID: {}, 权限数量: {}", roleId, 
                permissionIds != null ? permissionIds.size() : 0);
        permissionService.updateRolePermissions(roleId, permissionIds != null ? permissionIds : List.of());
        return Result.success();
    }
}

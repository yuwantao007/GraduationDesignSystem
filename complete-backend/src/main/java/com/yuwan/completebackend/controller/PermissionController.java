package com.yuwan.completebackend.controller;

import com.yuwan.completebackend.common.Result;
import com.yuwan.completebackend.model.vo.PermissionVO;
import com.yuwan.completebackend.service.IPermissionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 权限管理控制器
 * 提供权限信息查询接口
 *
 * @author 系统架构师
 * @version 1.0
 * @since 2026-02-24
 */
@Slf4j
@RestController
@RequestMapping("/permission")
@RequiredArgsConstructor
@Tag(name = "权限管理接口", description = "权限信息查询")
public class PermissionController {

    private final IPermissionService permissionService;

    /**
     * 获取权限树形结构
     *
     * @return 权限树列表
     */
    @GetMapping("/tree")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    @Operation(summary = "获取权限树", description = "获取所有权限的树形结构，用于角色权限配置")
    public Result<List<PermissionVO>> getPermissionTree() {
        log.info("获取权限树形结构");
        List<PermissionVO> tree = permissionService.getPermissionTree();
        return Result.success(tree);
    }

    /**
     * 获取所有权限列表（扁平结构）
     *
     * @return 权限列表
     */
    @GetMapping("/list")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    @Operation(summary = "获取权限列表", description = "获取所有权限的扁平列表")
    public Result<List<PermissionVO>> getAllPermissions() {
        log.info("获取权限列表");
        List<PermissionVO> permissions = permissionService.getAllPermissions();
        return Result.success(permissions);
    }
}

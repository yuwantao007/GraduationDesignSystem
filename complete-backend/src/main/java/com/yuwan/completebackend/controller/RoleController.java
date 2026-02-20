package com.yuwan.completebackend.controller;

import com.yuwan.completebackend.common.Result;
import com.yuwan.completebackend.model.vo.RoleVO;
import com.yuwan.completebackend.service.IRoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 角色管理Controller
 * 提供角色查询接口
 *
 * @author 系统架构师
 * @version 1.0
 * @since 2026-02-20
 */
@Slf4j
@RestController
@RequestMapping("/role")
@RequiredArgsConstructor
@Tag(name = "角色管理接口", description = "角色信息查询")
public class RoleController {

    private final IRoleService roleService;

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
}

package com.yuwan.completebackend.controller;

import com.yuwan.completebackend.common.PageResult;
import com.yuwan.completebackend.common.Result;
import com.yuwan.completebackend.model.dto.AssignRoleDTO;
import com.yuwan.completebackend.model.dto.ChangePasswordDTO;
import com.yuwan.completebackend.model.dto.CreateUserDTO;
import com.yuwan.completebackend.model.dto.UpdateUserDTO;
import com.yuwan.completebackend.model.vo.UserQueryVO;
import com.yuwan.completebackend.model.vo.UserVO;
import com.yuwan.completebackend.security.SecurityUtil;
import com.yuwan.completebackend.service.IUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 用户管理Controller
 * 提供用户CRUD、角色分配、密码管理等接口
 *
 * @author 系统架构师
 * @version 1.0
 * @since 2026-02-20
 */
@Slf4j
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
@Tag(name = "用户管理接口", description = "用户信息增删改查、角色分配、密码管理")
public class UserController {

    private final IUserService userService;

    @PostMapping("/create")
    @Operation(summary = "创建用户", description = "管理员创建新用户")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    public Result<UserVO> createUser(@RequestBody @Valid CreateUserDTO createDTO) {
        UserVO userVO = userService.createUser(createDTO);
        return Result.success("创建用户成功", userVO);
    }

    @PutMapping("/{userId}")
    @Operation(summary = "更新用户信息", description = "更新指定用户的基本信息")
    @PreAuthorize("hasRole('SYSTEM_ADMIN') or #userId == authentication.principal.userId")
    public Result<UserVO> updateUser(
            @Parameter(description = "用户ID") @PathVariable String userId,
            @RequestBody @Valid UpdateUserDTO updateDTO) {
        UserVO userVO = userService.updateUser(userId, updateDTO);
        return Result.success("更新用户成功", userVO);
    }

    @GetMapping("/{userId}")
    @Operation(summary = "获取用户详情", description = "获取指定用户的详细信息")
    public Result<UserVO> getUserDetail(
            @Parameter(description = "用户ID") @PathVariable String userId) {
        UserVO userVO = userService.getUserDetail(userId);
        return Result.success(userVO);
    }

    @GetMapping("/list")
    @Operation(summary = "分页查询用户列表", description = "根据条件分页查询用户")
    @PreAuthorize("hasRole('SYSTEM_ADMIN') or hasRole('ENTERPRISE_LEADER')")
    public Result<PageResult<UserVO>> getUserList(UserQueryVO queryVO) {
        PageResult<UserVO> result = userService.getUserList(queryVO);
        return Result.success(result);
    }

    @DeleteMapping("/{userId}")
    @Operation(summary = "删除用户", description = "逻辑删除指定用户")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    public Result<Void> deleteUser(
            @Parameter(description = "用户ID") @PathVariable String userId) {
        userService.deleteUser(userId);
        return Result.success("删除用户成功", null);
    }

    @PutMapping("/{userId}/status/{status}")
    @Operation(summary = "修改用户状态", description = "启用/禁用/锁定用户")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    public Result<Void> updateUserStatus(
            @Parameter(description = "用户ID") @PathVariable String userId,
            @Parameter(description = "目标状态") @PathVariable Integer status) {
        userService.updateUserStatus(userId, status);
        return Result.success("修改状态成功", null);
    }

    @PutMapping("/change-password")
    @Operation(summary = "修改密码", description = "当前用户修改自己的密码")
    public Result<Void> changePassword(@RequestBody @Valid ChangePasswordDTO changePasswordDTO) {
        String userId = SecurityUtil.getCurrentUserId();
        userService.changePassword(userId, changePasswordDTO);
        return Result.success("修改密码成功，请重新登录", null);
    }

    @PutMapping("/{userId}/reset-password")
    @Operation(summary = "重置密码", description = "管理员重置用户密码为默认密码")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    public Result<Void> resetPassword(
            @Parameter(description = "用户ID") @PathVariable String userId) {
        userService.resetPassword(userId);
        return Result.success("重置密码成功，默认密码为123456", null);
    }

    @PostMapping("/assign-roles")
    @Operation(summary = "分配角色", description = "为指定用户分配角色")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    public Result<Void> assignRoles(@RequestBody @Valid AssignRoleDTO assignRoleDTO) {
        userService.assignRoles(assignRoleDTO);
        return Result.success("分配角色成功", null);
    }

    @GetMapping("/current")
    @Operation(summary = "获取当前用户信息", description = "获取当前登录用户的详细信息")
    public Result<UserVO> getCurrentUserInfo() {
        String userId = SecurityUtil.getCurrentUserId();
        UserVO userVO = userService.getCurrentUserInfo(userId);
        return Result.success(userVO);
    }
}

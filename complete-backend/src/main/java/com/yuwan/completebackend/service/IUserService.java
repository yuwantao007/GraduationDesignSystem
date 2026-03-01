package com.yuwan.completebackend.service;

import com.yuwan.completebackend.common.PageResult;
import com.yuwan.completebackend.model.dto.AssignRoleDTO;
import com.yuwan.completebackend.model.dto.ChangePasswordDTO;
import com.yuwan.completebackend.model.dto.CreateUserDTO;
import com.yuwan.completebackend.model.dto.UpdateUserDTO;
import com.yuwan.completebackend.model.vo.UserQueryVO;
import com.yuwan.completebackend.model.vo.UserVO;

/**
 * 用户管理服务接口
 * 提供用户CRUD、角色分配、密码管理等功能
 *
 * @author 系统架构师
 * @version 1.0
 * @since 2026-02-20
 */
public interface IUserService {

    /**
     * 创建用户
     *
     * @param createDTO 创建用户参数
     * @return 用户信息
     */
    UserVO createUser(CreateUserDTO createDTO);

    /**
     * 更新用户信息
     *
     * @param userId    用户ID
     * @param updateDTO 更新参数
     * @return 更新后的用户信息
     */
    UserVO updateUser(String userId, UpdateUserDTO updateDTO);

    /**
     * 获取用户详情
     *
     * @param userId 用户ID
     * @return 用户详细信息
     */
    UserVO getUserDetail(String userId);

    /**
     * 分页查询用户列表
     *
     * @param queryVO 查询参数
     * @return 分页结果
     */
    PageResult<UserVO> getUserList(UserQueryVO queryVO);

    /**
     * 删除用户（逻辑删除）
     *
     * @param userId 用户ID
     */
    void deleteUser(String userId);

    /**
     * 修改用户状态
     *
     * @param userId 用户ID
     * @param status 目标状态（0-禁用 1-正常 2-锁定）
     */
    void updateUserStatus(String userId, Integer status);

    /**
     * 修改密码
     *
     * @param userId            用户ID
     * @param changePasswordDTO 修改密码参数
     */
    void changePassword(String userId, ChangePasswordDTO changePasswordDTO);

    /**
     * 重置密码
     *
     * @param userId 用户ID
     */
    void resetPassword(String userId);

    /**
     * 分配用户角色
     *
     * @param assignRoleDTO 角色分配参数
     */
    void assignRoles(AssignRoleDTO assignRoleDTO);

    /**
     * 获取当前登录用户信息
     *
     * @param userId 用户ID
     * @return 用户信息
     */
    UserVO getCurrentUserInfo(String userId);

    /**
     * 获取负责人列表
     * 返回可以作为负责人的用户列表（排除学生和系统管理员角色）
     *
     * @return 负责人列表
     */
    java.util.List<UserVO> getLeaderList();
}

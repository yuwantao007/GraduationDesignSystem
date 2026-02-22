package com.yuwan.completebackend.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 用户信息响应VO
 *
 * @author 系统架构师
 * @version 1.0
 * @since 2026-02-20
 */
@Data
@Schema(description = "用户信息响应")
public class UserVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "用户ID")
    private String userId;

    @Schema(description = "登录账号")
    private String username;

    @Schema(description = "真实姓名")
    private String realName;

    @Schema(description = "用户邮箱")
    private String userEmail;

    @Schema(description = "手机号码")
    private String userPhone;

    @Schema(description = "头像地址")
    private String avatar;

    @Schema(description = "性别（1-男 0-女）")
    private Integer gender;

    @Schema(description = "所属院系/企业")
    private String department;

    @Schema(description = "专业方向")
    private String major;

    @Schema(description = "学号")
    private String studentNo;

    @Schema(description = "工号")
    private String employeeNo;

    @Schema(description = "职称")
    private String title;

    @Schema(description = "账号状态（0-禁用 1-正常 2-锁定）")
    private Integer userStatus;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "最后登录时间")
    private String lastLoginTime;

    @Schema(description = "创建时间")
    private String createTime;

    @Schema(description = "角色列表")
    private List<RoleVO> roles;

    @Schema(description = "权限编码列表")
    private List<String> permissions;
}

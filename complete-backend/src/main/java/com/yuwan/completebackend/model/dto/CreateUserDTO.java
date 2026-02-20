package com.yuwan.completebackend.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 创建用户请求DTO
 * 管理员创建用户时使用
 *
 * @author 系统架构师
 * @version 1.0
 * @since 2026-02-20
 */
@Data
@Schema(description = "创建用户请求参数")
public class CreateUserDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotBlank(message = "登录账号不能为空")
    @Size(min = 4, max = 30, message = "登录账号长度必须在4-30个字符之间")
    @Schema(description = "登录账号", requiredMode = Schema.RequiredMode.REQUIRED)
    private String username;

    @NotBlank(message = "登录密码不能为空")
    @Size(min = 6, max = 20, message = "登录密码长度必须在6-20个字符之间")
    @Schema(description = "登录密码", requiredMode = Schema.RequiredMode.REQUIRED)
    private String password;

    @NotBlank(message = "真实姓名不能为空")
    @Size(min = 2, max = 20, message = "真实姓名长度必须在2-20个字符之间")
    @Schema(description = "真实姓名", requiredMode = Schema.RequiredMode.REQUIRED)
    private String realName;

    @Email(message = "邮箱格式不正确")
    @Schema(description = "用户邮箱")
    private String userEmail;

    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号码格式不正确")
    @Schema(description = "手机号码")
    private String userPhone;

    @Schema(description = "性别（0-未知 1-男 2-女）")
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

    @Schema(description = "备注")
    private String remark;

    @NotEmpty(message = "角色列表不能为空")
    @Schema(description = "角色ID列表", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<String> roleIds;
}

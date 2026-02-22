package com.yuwan.completebackend.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 用户注册请求DTO
 *
 * @author 系统架构师
 * @version 1.0
 * @since 2026-02-20
 */
@Data
@Schema(description = "用户注册请求参数")
public class RegisterDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Size(max = 30, message = "学号长度不能超过30个字符")
    @Schema(description = "学号（学生角色必填）")
    private String studentNo;

    @Size(max = 30, message = "工号长度不能超过30个字符")
    @Schema(description = "工号（教师角色必填）")
    private String employeeNo;

    @NotBlank(message = "登录密码不能为空")
    @Size(min = 6, max = 20, message = "登录密码长度必须在6-20个字符之间")
    @Schema(description = "登录密码", requiredMode = Schema.RequiredMode.REQUIRED)
    private String password;

    @NotBlank(message = "确认密码不能为空")
    @Schema(description = "确认密码", requiredMode = Schema.RequiredMode.REQUIRED)
    private String confirmPassword;

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

    @NotBlank(message = "角色编码不能为空")
    @Schema(description = "角色编码", requiredMode = Schema.RequiredMode.REQUIRED)
    private String roleCode;
}

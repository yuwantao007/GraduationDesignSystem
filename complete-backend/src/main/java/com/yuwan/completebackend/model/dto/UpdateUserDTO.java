package com.yuwan.completebackend.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 更新用户信息请求DTO
 *
 * @author 系统架构师
 * @version 1.0
 * @since 2026-02-20
 */
@Data
@Schema(description = "更新用户信息请求参数")
public class UpdateUserDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Size(min = 2, max = 20, message = "真实姓名长度必须在2-20个字符之间")
    @Schema(description = "真实姓名")
    private String realName;

    @Email(message = "邮箱格式不正确")
    @Schema(description = "用户邮箱")
    private String userEmail;

    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号码格式不正确")
    @Schema(description = "手机号码")
    private String userPhone;

    @Schema(description = "头像地址")
    private String avatar;

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

    @Schema(description = "角色ID列表")
    private List<String> roleIds;
}

package com.yuwan.completebackend.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 更新学校请求DTO
 * 管理员更新学校信息时使用
 *
 * @author 系统架构师
 * @version 1.0
 * @since 2026-02-22
 */
@Data
@Schema(description = "更新学校请求参数")
public class UpdateSchoolDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Size(max = 100, message = "学校名称长度不能超过100个字符")
    @Schema(description = "学校名称")
    private String schoolName;

    @Size(max = 50, message = "学校编码长度不能超过50个字符")
    @Schema(description = "学校编码")
    private String schoolCode;

    @Size(max = 200, message = "详细地址长度不能超过200个字符")
    @Schema(description = "详细地址")
    private String address;

    @Size(max = 50, message = "联系人姓名长度不能超过50个字符")
    @Schema(description = "联系人")
    private String contactPerson;

    @Pattern(regexp = "^(1[3-9]\\d{9}|0\\d{2,3}-?\\d{7,8})$", message = "联系电话格式不正确")
    @Schema(description = "联系电话")
    private String contactPhone;

    @Email(message = "学校邮箱格式不正确")
    @Schema(description = "学校邮箱")
    private String schoolEmail;

    @Size(max = 500, message = "学校简介长度不能超过500个字符")
    @Schema(description = "学校简介")
    private String description;

    @Min(value = 0, message = "状态值无效")
    @Max(value = 1, message = "状态值无效")
    @Schema(description = "状态（0-禁用 1-正常）")
    private Integer schoolStatus;
}

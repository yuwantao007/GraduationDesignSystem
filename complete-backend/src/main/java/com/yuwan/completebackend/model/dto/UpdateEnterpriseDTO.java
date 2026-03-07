package com.yuwan.completebackend.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 更新企业请求DTO
 * 管理员更新企业信息时使用
 *
 * @author 系统架构师
 * @version 1.0
 * @since 2026-02-21
 */
@Data
@Schema(description = "更新企业请求参数")
public class UpdateEnterpriseDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Size(max = 100, message = "企业名称长度不能超过100个字符")
    @Schema(description = "企业名称")
    private String enterpriseName;

    @Size(max = 50, message = "企业编码长度不能超过50个字符")
    @Schema(description = "企业编码")
    private String enterpriseCode;

    @Schema(description = "企业负责人用户ID")
    private String leaderId;

    @Size(max = 50, message = "联系人姓名长度不能超过50个字符")
    @Schema(description = "联系人（由负责人姓名自动填充，也可手动填写）")
    private String contactPerson;

    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "联系电话格式不正确")
    @Schema(description = "联系电话")
    private String contactPhone;

    @Email(message = "联系邮箱格式不正确")
    @Schema(description = "联系邮箱")
    private String contactEmail;

    @Size(max = 200, message = "企业地址长度不能超过200个字符")
    @Schema(description = "企业地址")
    private String address;

    @Size(max = 500, message = "企业简介长度不能超过500个字符")
    @Schema(description = "企业简介")
    private String description;

    @Schema(description = "状态（0-禁用 1-正常）")
    private Integer enterpriseStatus;
}

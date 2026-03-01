package com.yuwan.completebackend.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 专业方向创建/更新DTO
 * 用于接收前端提交的专业方向表单数据
 *
 * @author 系统架构师
 * @version 1.0
 * @since 2026-03-01
 */
@Data
@Schema(description = "专业方向表单参数")
public class MajorDirectionDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "企业ID（系统管理员创建时必填）")
    private String enterpriseId;

    @NotBlank(message = "专业方向名称不能为空")
    @Schema(description = "专业方向名称", requiredMode = Schema.RequiredMode.REQUIRED)
    private String directionName;

    @Schema(description = "专业方向代码")
    private String directionCode;

    @Schema(description = "专业方向描述")
    private String description;

    @Schema(description = "方向负责人ID")
    private String leaderId;

    @Schema(description = "排序", defaultValue = "0")
    private Integer sortOrder;

    @Schema(description = "状态（1-启用 0-禁用）", defaultValue = "1")
    private Integer status;
}

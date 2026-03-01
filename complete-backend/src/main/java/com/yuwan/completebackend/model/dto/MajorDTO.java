package com.yuwan.completebackend.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 专业创建/更新DTO
 * 用于接收前端提交的专业表单数据
 *
 * @author 系统架构师
 * @version 1.0
 * @since 2026-03-01
 */
@Data
@Schema(description = "专业表单参数")
public class MajorDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotBlank(message = "所属专业方向不能为空")
    @Schema(description = "所属专业方向ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private String directionId;

    @NotBlank(message = "专业名称不能为空")
    @Schema(description = "专业名称", requiredMode = Schema.RequiredMode.REQUIRED)
    private String majorName;

    @Schema(description = "专业代码")
    private String majorCode;

    @Schema(description = "学位类型（本科/专科）")
    private String degreeType;

    @Schema(description = "学制（年）", defaultValue = "4")
    private Integer educationYears;

    @Schema(description = "专业描述")
    private String description;

    @Schema(description = "排序", defaultValue = "0")
    private Integer sortOrder;

    @Schema(description = "状态（1-启用 0-禁用）", defaultValue = "1")
    private Integer status;
}

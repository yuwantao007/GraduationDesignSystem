package com.yuwan.completebackend.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 综合审查意见请求DTO
 *
 * @author 系统架构师
 * @version 1.0
 * @since 2026-02-23
 */
@Data
@Schema(description = "综合审查意见请求参数")
public class GeneralOpinionDTO {

    @NotNull(message = "审查阶段不能为空")
    @Min(value = 1, message = "审查阶段无效")
    @Max(value = 3, message = "审查阶段无效")
    @Schema(description = "审查阶段（1-预审 2-初审 3-终审）", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer reviewStage;

    @NotBlank(message = "专业方向不能为空")
    @Size(max = 100, message = "专业方向不能超过100字")
    @Schema(description = "适用专业方向", requiredMode = Schema.RequiredMode.REQUIRED)
    private String guidanceDirection;

    @NotBlank(message = "综合意见内容不能为空")
    @Size(max = 200, message = "综合意见不能超过200字")
    @Schema(description = "综合意见内容（≤200字）", requiredMode = Schema.RequiredMode.REQUIRED)
    private String opinionContent;
}

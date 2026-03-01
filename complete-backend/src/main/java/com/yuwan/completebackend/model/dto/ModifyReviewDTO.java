package com.yuwan.completebackend.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 修改审查结果请求DTO
 *
 * @author 系统架构师
 * @version 1.0
 * @since 2026-02-23
 */
@Data
@Schema(description = "修改审查结果请求参数")
public class ModifyReviewDTO {

    @NotBlank(message = "审查记录ID不能为空")
    @Schema(description = "审查记录ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private String reviewId;

    @NotNull(message = "新审查结果不能为空")
    @Min(value = 1, message = "审查结果无效")
    @Max(value = 3, message = "审查结果无效")
    @Schema(description = "新审查结果（1-通过 2-需修改 3-不通过）", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer newReviewResult;

    @Size(max = 500, message = "修改原因不能超过500字")
    @Schema(description = "修改原因说明")
    private String modifyReason;

    @Size(max = 500, message = "新审查意见不能超过500字")
    @Schema(description = "新审查意见（可选，不填则保留原意见）")
    private String newReviewOpinion;
}

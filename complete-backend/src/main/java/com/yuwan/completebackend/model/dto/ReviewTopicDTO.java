package com.yuwan.completebackend.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 单个课题审批请求DTO
 *
 * @author 系统架构师
 * @version 1.0
 * @since 2026-02-23
 */
@Data
@Schema(description = "单个课题审批请求参数")
public class ReviewTopicDTO {

    @NotBlank(message = "课题ID不能为空")
    @Schema(description = "课题ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private String topicId;

    @NotNull(message = "审查结果不能为空")
    @Min(value = 1, message = "审查结果无效")
    @Max(value = 3, message = "审查结果无效")
    @Schema(description = "审查结果（1-通过 2-需修改 3-不通过，不通过仅终审可用）", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer reviewResult;

    @Size(max = 500, message = "审查意见不能超过500字")
    @Schema(description = "审查意见（针对单个课题，最多500字）")
    private String reviewOpinion;
}

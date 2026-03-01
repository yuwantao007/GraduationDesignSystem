package com.yuwan.completebackend.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * 批量课题审批请求DTO
 *
 * @author 系统架构师
 * @version 1.0
 * @since 2026-02-23
 */
@Data
@Schema(description = "批量课题审批请求参数")
public class BatchReviewDTO {

    @NotEmpty(message = "课题ID列表不能为空")
    @Size(max = 100, message = "单次批量审批不能超过100个课题")
    @Schema(description = "课题ID列表", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<String> topicIds;

    @NotNull(message = "审查结果不能为空")
    @Min(value = 1, message = "审查结果无效")
    @Max(value = 3, message = "审查结果无效")
    @Schema(description = "审查结果（1-通过 2-需修改 3-不通过，不通过仅终审可用）", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer reviewResult;

    @Size(max = 500, message = "审查意见不能超过500字")
    @Schema(description = "批量审查意见（应用于所有选中课题，最多500字）")
    private String reviewOpinion;
}

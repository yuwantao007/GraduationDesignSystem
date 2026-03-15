package com.yuwan.completebackend.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * 完成审查任务 DTO
 *
 * @author 系统架构师
 * @version 1.0
 * @since 2026-03-15
 */
@Data
@Schema(description = "完成审查任务请求参数")
public class CompleteReviewTaskDTO {

    @NotBlank(message = "审查结果不能为空")
    @Pattern(regexp = "PASS|NEED_MODIFY|REJECT", message = "审查结果只能是 PASS、NEED_MODIFY 或 REJECT")
    @Schema(description = "审查结果（PASS-通过 / NEED_MODIFY-需修改 / REJECT-终审不通过）",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String outcome;

    @Schema(description = "审查意见（NEED_MODIFY 和 REJECT 时建议填写）")
    private String opinion;
}

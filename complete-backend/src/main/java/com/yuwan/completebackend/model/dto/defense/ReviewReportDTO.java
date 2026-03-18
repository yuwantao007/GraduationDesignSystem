package com.yuwan.completebackend.model.dto.defense;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 审查开题报告DTO
 *
 * @author 系统架构师
 * @version 1.0
 * @since 2026-03-17
 */
@Data
@Schema(description = "审查开题报告请求对象")
public class ReviewReportDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotBlank(message = "报告ID不能为空")
    @Schema(description = "报告ID")
    private String reportId;

    @NotNull(message = "审查状态不能为空")
    @Schema(description = "审查状态: 2=通过, 3=不合格", example = "2")
    private Integer reviewStatus;

    @Schema(description = "审查意见")
    private String reviewComment;
}

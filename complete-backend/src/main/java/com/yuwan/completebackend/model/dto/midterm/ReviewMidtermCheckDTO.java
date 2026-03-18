package com.yuwan.completebackend.model.dto.midterm;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 审查中期检查表DTO
 *
 * @author 系统架构师
 * @version 1.0
 * @since 2026-03-17
 */
@Data
@Schema(description = "审查中期检查表请求对象")
public class ReviewMidtermCheckDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotBlank(message = "检查表ID不能为空")
    @Schema(description = "检查表ID")
    private String checkId;

    @NotNull(message = "审查状态不能为空")
    @Schema(description = "审查状态: 1=合格, 2=不合格")
    private Integer reviewStatus;

    @Schema(description = "审查意见")
    private String reviewComment;
}

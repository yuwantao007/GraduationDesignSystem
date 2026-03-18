package com.yuwan.completebackend.model.dto.midterm;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 创建/编辑中期检查表DTO
 *
 * @author 系统架构师
 * @version 1.0
 * @since 2026-03-17
 */
@Data
@Schema(description = "创建/编辑中期检查表请求对象")
public class CreateMidtermCheckDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "检查表ID（编辑时必传）")
    private String checkId;

    @NotBlank(message = "学生ID不能为空")
    @Schema(description = "学生ID")
    private String studentId;

    @NotBlank(message = "课题ID不能为空")
    @Schema(description = "课题ID")
    private String topicId;

    @Schema(description = "对应中期答辩安排ID")
    private String arrangementId;

    @Schema(description = "完成情况")
    private String completionStatus;

    @Schema(description = "存在问题")
    private String existingProblems;

    @Schema(description = "下一步计划")
    private String nextPlan;

    @Schema(description = "中期检查表附件ID")
    private String documentId;
}

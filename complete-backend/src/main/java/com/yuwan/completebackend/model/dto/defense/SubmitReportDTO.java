package com.yuwan.completebackend.model.dto.defense;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * 提交开题报告DTO
 *
 * @author 系统架构师
 * @version 1.0
 * @since 2026-03-17
 */
@Data
@Schema(description = "提交开题报告请求对象")
public class SubmitReportDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotBlank(message = "课题ID不能为空")
    @Schema(description = "课题ID")
    private String topicId;

    @Schema(description = "对应答辩安排ID")
    private String arrangementId;

    @Schema(description = "姓名快照")
    private String studentName;

    @Schema(description = "专业快照")
    private String majorName;

    @Schema(description = "班级快照")
    private String className;

    @Schema(description = "题目快照")
    private String topicTitle;

    @Schema(description = "指导教师姓名（可多名，逗号分隔）")
    private String advisorNames;

    @Schema(description = "报告日期")
    private Date reportDate;

    @Schema(description = "国内外研究现状")
    private String researchStatus;

    @Schema(description = "研究目的、意义")
    private String purposeSignificance;

    @Schema(description = "研究内容")
    private String researchContent;

    @Schema(description = "课题研究创新点")
    private String innovationPoints;

    @Schema(description = "拟解决问题")
    private String problemsToSolve;

    @Schema(description = "进度及预期结果")
    private String progressExpectation;

    @Schema(description = "完成题目的现有条件")
    private String currentConditions;

    @Schema(description = "指导教师意见")
    private String advisorOpinion;

    @Schema(description = "学院意见")
    private String collegeOpinion;

    @NotNull(message = "状态不能为空")
    @Schema(description = "状态: 0=草稿, 1=已定稿", example = "0")
    private Integer status;
}

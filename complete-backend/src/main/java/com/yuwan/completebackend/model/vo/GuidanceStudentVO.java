package com.yuwan.completebackend.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 教师视角的学生信息（含最新指导时间）
 *
 * @author 系统架构师
 * @version 1.0
 * @since 2026-03-16
 */
@Data
@Schema(description = "教师视角的学生信息")
public class GuidanceStudentVO {

    @Schema(description = "学生用户ID")
    private String studentId;

    @Schema(description = "学生姓名")
    private String studentName;

    @Schema(description = "学号")
    private String studentNo;

    @Schema(description = "学生手机号")
    private String studentPhone;

    @Schema(description = "学生邮箱")
    private String studentEmail;

    @Schema(description = "课题ID")
    private String topicId;

    @Schema(description = "课题名称")
    private String topicTitle;

    @Schema(description = "指导记录数量")
    private Integer recordCount;

    @Schema(description = "最新指导日期")
    private String lastGuidanceDate;

    @Schema(description = "总指导时长（小时）")
    private String totalHours;
}

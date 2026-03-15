package com.yuwan.completebackend.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 高校教师视角的指导学生选题结果
 * <p>
 * 高校教师通过 teacher_relationship 与企业教师精确配对，
 * 可查看配对企业教师名下所有学生的选报记录及结果。
 * </p>
 *
 * @author 系统架构师
 * @version 1.0
 * @since 2026-03-14
 */
@Data
@Schema(description = "高校教师视角选题结果")
public class SelectionForUnivTeacherVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    // ===== 选报记录信息 =====

    @Schema(description = "选报ID")
    private String selectionId;

    @Schema(description = "选报状态（0-待确认 1-中选 2-落选）")
    private Integer selectionStatus;

    @Schema(description = "选报状态描述")
    private String selectionStatusDesc;

    @Schema(description = "选报理由")
    private String selectionReason;

    @Schema(description = "选报时间")
    private String applyTime;

    @Schema(description = "确认时间")
    private String confirmTime;

    // ===== 学生信息 =====

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

    // ===== 课题信息 =====

    @Schema(description = "课题ID")
    private String topicId;

    @Schema(description = "课题名称")
    private String topicTitle;

    @Schema(description = "课题大类（1-高职升本 2-3+1 3-实验班）")
    private Integer topicCategory;

    @Schema(description = "课题大类描述")
    private String topicCategoryDesc;

    @Schema(description = "课题来源（1-校内 2-校外协同开发）")
    private Integer topicSource;

    @Schema(description = "课题来源描述")
    private String topicSourceDesc;

    @Schema(description = "指导方向")
    private String guidanceDirection;

    // ===== 企业教师信息 =====

    @Schema(description = "企业教师ID")
    private String enterpriseTeacherId;

    @Schema(description = "企业教师姓名")
    private String enterpriseTeacherName;

    @Schema(description = "企业名称")
    private String enterpriseName;
}

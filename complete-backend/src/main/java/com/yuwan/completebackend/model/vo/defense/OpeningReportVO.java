package com.yuwan.completebackend.model.vo.defense;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * 开题报告VO
 *
 * @author 系统架构师
 * @version 1.0
 * @since 2026-03-17
 */
@Data
@Schema(description = "开题报告返回对象")
public class OpeningReportVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "报告ID")
    private String reportId;

    @Schema(description = "学生ID")
    private String studentId;

    @Schema(description = "学生姓名")
    private String studentName;

    @Schema(description = "学号")
    private String studentNo;

    @Schema(description = "专业名称")
    private String majorName;

    @Schema(description = "班级名称")
    private String className;

    @Schema(description = "课题ID")
    private String topicId;

    @Schema(description = "课题名称")
    private String topicName;

    @Schema(description = "指导教师")
    private String advisorNames;

    @Schema(description = "报告日期")
    private Date reportDate;

    @Schema(description = "对应答辩安排ID")
    private String arrangementId;

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

    @Schema(description = "提交时间")
    private Date submitTime;

    @Schema(description = "状态: 0=草稿, 1=已定稿")
    private Integer status;

    @Schema(description = "状态名称")
    private String statusName;

    @Schema(description = "创建时间")
    private Date createTime;

    @Schema(description = "更新时间")
    private Date updateTime;
}

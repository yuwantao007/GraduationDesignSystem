package com.yuwan.completebackend.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * 开题报告实体类
 * 对应数据库表 opening_report
 *
 * @author 系统架构师
 * @version 1.0
 * @since 2026-03-17
 */
@Data
@TableName("opening_report")
@Schema(description = "开题报告实体")
public class OpeningReport implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "report_id", type = IdType.ASSIGN_ID)
    @Schema(description = "报告ID")
    private String reportId;

    @Schema(description = "学生ID（一个学生只有一份）")
    private String studentId;

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

    @Schema(description = "提交时间")
    private Date submitTime;

    @Schema(description = "状态: 0=草稿, 1=已定稿")
    private Integer status;

    @TableLogic
    @TableField("is_deleted")
    @Schema(description = "逻辑删除: 0=正常, 1=删除")
    private Integer deleted;

    @TableField(fill = FieldFill.INSERT)
    @Schema(description = "创建时间")
    private Date createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    @Schema(description = "更新时间")
    private Date updateTime;
}


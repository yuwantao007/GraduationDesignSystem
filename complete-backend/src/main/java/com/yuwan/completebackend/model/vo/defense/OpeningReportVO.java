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

    @Schema(description = "课题ID")
    private String topicId;

    @Schema(description = "课题名称")
    private String topicName;

    @Schema(description = "对应答辩安排ID")
    private String arrangementId;

    @Schema(description = "报告文件ID")
    private String documentId;

    @Schema(description = "报告文件名称")
    private String documentName;

    @Schema(description = "报告文件URL")
    private String documentUrl;

    @Schema(description = "提交时间")
    private Date submitTime;

    @Schema(description = "审查状态: 0=未提交, 1=已提交待审, 2=通过, 3=不合格")
    private Integer reviewStatus;

    @Schema(description = "审查状态名称")
    private String reviewStatusName;

    @Schema(description = "审查意见")
    private String reviewComment;

    @Schema(description = "审查人ID")
    private String reviewerId;

    @Schema(description = "审查人姓名")
    private String reviewerName;

    @Schema(description = "审查时间")
    private Date reviewTime;

    @Schema(description = "创建时间")
    private Date createTime;

    @Schema(description = "更新时间")
    private Date updateTime;
}

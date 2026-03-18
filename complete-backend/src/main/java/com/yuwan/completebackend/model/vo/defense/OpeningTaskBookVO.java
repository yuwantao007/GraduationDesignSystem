package com.yuwan.completebackend.model.vo.defense;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * 开题任务书VO
 *
 * @author 系统架构师
 * @version 1.0
 * @since 2026-03-17
 */
@Data
@Schema(description = "开题任务书返回对象")
public class OpeningTaskBookVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "任务书ID")
    private String taskBookId;

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

    @Schema(description = "编辑教师ID")
    private String teacherId;

    @Schema(description = "编辑教师姓名")
    private String teacherName;

    @Schema(description = "任务书正文（富文本HTML）")
    private String content;

    @Schema(description = "任务书附件ID")
    private String documentId;

    @Schema(description = "任务书附件名称")
    private String documentName;

    @Schema(description = "任务书附件URL")
    private String documentUrl;

    @Schema(description = "创建时间")
    private Date createTime;

    @Schema(description = "更新时间")
    private Date updateTime;
}

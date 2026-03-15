package com.yuwan.completebackend.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 教师指派记录响应VO
 * 用于企业负责人查看指派列表
 *
 * @author 系统架构师
 * @version 1.0
 * @since 2026-03-14
 */
@Data
@Schema(description = "教师指派记录响应")
public class TeacherAssignmentVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "指派ID")
    private String assignmentId;

    @Schema(description = "学生用户ID")
    private String studentId;

    @Schema(description = "学生姓名")
    private String studentName;

    @Schema(description = "学号")
    private String studentNo;

    @Schema(description = "课题ID")
    private String topicId;

    @Schema(description = "课题名称")
    private String topicTitle;

    @Schema(description = "课题来源（1-校内 2-校外协同开发）")
    private Integer topicSource;

    @Schema(description = "课题来源描述")
    private String topicSourceDesc;

    @Schema(description = "指派教师ID")
    private String assignedTeacherId;

    @Schema(description = "指派教师姓名")
    private String assignedTeacherName;

    @Schema(description = "指派人姓名（企业负责人）")
    private String assignedByName;

    @Schema(description = "指派时间")
    private String assignTime;

    @Schema(description = "取消时间")
    private String cancelTime;

    @Schema(description = "指派状态（1-已指派 0-已取消）")
    private Integer assignStatus;

    @Schema(description = "指派状态描述")
    private String assignStatusDesc;
}

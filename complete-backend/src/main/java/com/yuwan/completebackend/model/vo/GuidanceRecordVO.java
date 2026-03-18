package com.yuwan.completebackend.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 指导记录详情响应
 *
 * @author 系统架构师
 * @version 1.0
 * @since 2026-03-16
 */
@Data
@Schema(description = "指导记录详情响应")
public class GuidanceRecordVO {

    @Schema(description = "指导记录ID")
    private String recordId;

    @Schema(description = "学生用户ID")
    private String studentId;

    @Schema(description = "学生姓名")
    private String studentName;

    @Schema(description = "学号")
    private String studentNo;

    @Schema(description = "指导教师ID")
    private String teacherId;

    @Schema(description = "指导教师姓名")
    private String teacherName;

    @Schema(description = "关联课题ID")
    private String topicId;

    @Schema(description = "课题名称")
    private String topicTitle;

    @Schema(description = "指导类型（1-项目指导 2-论文指导）")
    private Integer guidanceType;

    @Schema(description = "指导类型描述")
    private String guidanceTypeDesc;

    @Schema(description = "指导日期（yyyy-MM-dd）")
    private String guidanceDate;

    @Schema(description = "指导内容")
    private String guidanceContent;

    @Schema(description = "指导方式")
    private String guidanceMethod;

    @Schema(description = "指导时长（小时）")
    private BigDecimal durationHours;

    @Schema(description = "创建时间")
    private String createTime;

    @Schema(description = "更新时间")
    private String updateTime;
}

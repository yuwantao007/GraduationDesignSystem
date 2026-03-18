package com.yuwan.completebackend.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 指导记录查询参数
 *
 * @author 系统架构师
 * @version 1.0
 * @since 2026-03-16
 */
@Data
@Schema(description = "指导记录查询参数")
public class GuidanceQueryVO {

    @Schema(description = "学生用户ID")
    private String studentId;

    @Schema(description = "课题ID")
    private String topicId;

    @Schema(description = "指导类型（1-项目指导 2-论文指导）")
    private Integer guidanceType;

    @Schema(description = "学生姓名（模糊查询）")
    private String studentName;

    @Schema(description = "指导教师ID")
    private String teacherId;

    @Schema(description = "开始日期（yyyy-MM-dd）")
    private String startDate;

    @Schema(description = "结束日期（yyyy-MM-dd）")
    private String endDate;

    @Schema(description = "页码", example = "1")
    private Integer pageNum = 1;

    @Schema(description = "每页大小", example = "10")
    private Integer pageSize = 10;
}

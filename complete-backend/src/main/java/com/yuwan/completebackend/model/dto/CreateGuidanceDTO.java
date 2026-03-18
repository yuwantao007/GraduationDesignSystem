package com.yuwan.completebackend.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 创建指导记录请求参数
 *
 * @author 系统架构师
 * @version 1.0
 * @since 2026-03-16
 */
@Data
@Schema(description = "创建指导记录请求参数")
public class CreateGuidanceDTO {

    @NotBlank(message = "学生ID不能为空")
    @Schema(description = "学生用户ID")
    private String studentId;

    @NotBlank(message = "课题ID不能为空")
    @Schema(description = "关联课题ID")
    private String topicId;

    @NotNull(message = "指导类型不能为空")
    @Schema(description = "指导类型（1-项目指导 2-论文指导）")
    private Integer guidanceType;

    @NotNull(message = "指导日期不能为空")
    @Schema(description = "指导日期")
    private Date guidanceDate;

    @NotBlank(message = "指导内容不能为空")
    @Schema(description = "指导内容（必填）")
    private String guidanceContent;

    @Schema(description = "指导方式（线上/线下/邮件等）")
    private String guidanceMethod;

    @Schema(description = "指导时长（小时）")
    private BigDecimal durationHours;
}

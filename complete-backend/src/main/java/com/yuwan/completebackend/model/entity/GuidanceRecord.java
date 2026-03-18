package com.yuwan.completebackend.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 指导记录实体类
 * 对应数据库表 guidance_record
 *
 * @author 系统架构师
 * @version 1.0
 * @since 2026-03-16
 */
@Data
@TableName("guidance_record")
@Schema(description = "指导记录实体")
public class GuidanceRecord implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "record_id", type = IdType.ASSIGN_ID)
    @Schema(description = "指导记录ID")
    private String recordId;

    @Schema(description = "学生用户ID")
    private String studentId;

    @Schema(description = "指导教师ID")
    private String teacherId;

    @Schema(description = "关联课题ID")
    private String topicId;

    @Schema(description = "指导类型（1-项目指导 2-论文指导）")
    private Integer guidanceType;

    @Schema(description = "指导日期")
    private Date guidanceDate;

    @Schema(description = "指导内容（必填）")
    private String guidanceContent;

    @Schema(description = "指导方式（线上/线下/邮件等）")
    private String guidanceMethod;

    @Schema(description = "指导时长（小时）")
    private BigDecimal durationHours;

    @TableLogic
    @Schema(description = "逻辑删除（0-未删除 1-已删除）")
    private Integer deleted;

    @TableField(fill = FieldFill.INSERT)
    @Schema(description = "创建时间")
    private Date createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    @Schema(description = "更新时间")
    private Date updateTime;
}

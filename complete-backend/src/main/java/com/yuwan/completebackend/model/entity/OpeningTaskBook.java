package com.yuwan.completebackend.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * 开题任务书实体类
 * 对应数据库表 opening_task_book
 *
 * @author 系统架构师
 * @version 1.0
 * @since 2026-03-17
 */
@Data
@TableName("opening_task_book")
@Schema(description = "开题任务书实体")
public class OpeningTaskBook implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "task_book_id", type = IdType.ASSIGN_ID)
    @Schema(description = "任务书ID")
    private String taskBookId;

    @Schema(description = "学生ID（一个学生一份任务书）")
    private String studentId;

    @Schema(description = "课题ID")
    private String topicId;

    @Schema(description = "编辑教师ID（企业教师）")
    private String teacherId;

    @Schema(description = "任务书正文（富文本HTML）")
    private String content;

    @Schema(description = "任务书附件ID（关联document_info）")
    private String documentId;

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


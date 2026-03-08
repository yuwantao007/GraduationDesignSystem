package com.yuwan.completebackend.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * 课题选报记录实体类
 * 对应数据库表 topic_selection
 *
 * @author 系统架构师
 * @version 1.0
 * @since 2026-03-08
 */
@Data
@TableName("topic_selection")
@Schema(description = "课题选报记录实体")
public class TopicSelection implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "selection_id", type = IdType.ASSIGN_ID)
    @Schema(description = "选报ID")
    private String selectionId;

    @Schema(description = "学生用户ID")
    private String studentId;

    @Schema(description = "课题ID")
    private String topicId;

    @Schema(description = "选报理由")
    private String selectionReason;

    @Schema(description = "选报状态（0-待确认 1-中选 2-落选）")
    private Integer selectionStatus;

    @Schema(description = "选报时间")
    private Date applyTime;

    @Schema(description = "确认时间")
    private Date confirmTime;

    @Schema(description = "确认人ID（企业教师）")
    private String confirmedBy;

    @TableLogic
    @Schema(description = "逻辑删除（0-未删除 1-已删除）")
    private Integer deleted;

    @Schema(description = "创建时间")
    private Date createTime;

    @Schema(description = "更新时间")
    private Date updateTime;
}

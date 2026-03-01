package com.yuwan.completebackend.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * 课题审查记录实体类
 * 对应数据库表 topic_review_record
 *
 * @author 系统架构师
 * @version 1.0
 * @since 2026-02-23
 */
@Data
@TableName("topic_review_record")
@Schema(description = "课题审查记录实体")
public class TopicReviewRecord implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "review_id", type = IdType.ASSIGN_ID)
    @Schema(description = "审查记录ID")
    private String reviewId;

    @Schema(description = "课题ID")
    private String topicId;

    @Schema(description = "审查阶段（1-预审 2-初审 3-终审）")
    private Integer reviewStage;

    @Schema(description = "审查人ID")
    private String reviewerId;

    @Schema(description = "审查人角色代码")
    private String reviewerRole;

    @Schema(description = "审查人姓名")
    private String reviewerName;

    @Schema(description = "审查结果（1-通过 2-需修改 3-不通过）")
    private Integer reviewResult;

    @Schema(description = "审查意见（针对单个课题）")
    private String reviewOpinion;

    @Schema(description = "是否批量审查（0-否 1-是）")
    private Integer isBatchReview;

    @Schema(description = "批量审查批次ID")
    private String batchReviewId;

    @Schema(description = "审查前课题状态")
    private Integer previousStatus;

    @Schema(description = "审查后课题状态")
    private Integer newStatus;

    @Schema(description = "是否被修改过（0-否 1-是）")
    private Integer isModified;

    @Schema(description = "修改人ID")
    private String modifiedBy;

    @Schema(description = "修改时间")
    private Date modifiedTime;

    @TableLogic
    @Schema(description = "逻辑删除（0-未删除 1-已删除）")
    private Integer deleted;

    @TableField(fill = FieldFill.INSERT)
    @Schema(description = "审查时间")
    private Date createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    @Schema(description = "更新时间")
    private Date updateTime;
}

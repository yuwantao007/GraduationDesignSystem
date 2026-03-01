package com.yuwan.completebackend.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * 批量审查批次实体类
 * 对应数据库表 topic_batch_review
 *
 * @author 系统架构师
 * @version 1.0
 * @since 2026-02-23
 */
@Data
@TableName("topic_batch_review")
@Schema(description = "批量审查批次实体")
public class TopicBatchReview implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "batch_id", type = IdType.ASSIGN_ID)
    @Schema(description = "批次ID")
    private String batchId;

    @Schema(description = "审查人ID")
    private String reviewerId;

    @Schema(description = "审查人角色代码")
    private String reviewerRole;

    @Schema(description = "审查阶段（1-预审 2-初审 3-终审）")
    private Integer reviewStage;

    @Schema(description = "审查结果（1-通过 2-需修改 3-不通过）")
    private Integer reviewResult;

    @Schema(description = "批量审查意见")
    private String reviewOpinion;

    @Schema(description = "审查课题数量")
    private Integer topicCount;

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

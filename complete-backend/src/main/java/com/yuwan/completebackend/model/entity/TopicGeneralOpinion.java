package com.yuwan.completebackend.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * 综合审查意见实体类
 * 对应数据库表 topic_general_opinion
 *
 * @author 系统架构师
 * @version 1.0
 * @since 2026-02-23
 */
@Data
@TableName("topic_general_opinion")
@Schema(description = "综合审查意见实体")
public class TopicGeneralOpinion implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "opinion_id", type = IdType.ASSIGN_ID)
    @Schema(description = "意见ID")
    private String opinionId;

    @Schema(description = "提交人ID")
    private String reviewerId;

    @Schema(description = "提交人角色代码")
    private String reviewerRole;

    @Schema(description = "提交人姓名")
    private String reviewerName;

    @Schema(description = "审查阶段（1-预审 2-初审 3-终审）")
    private Integer reviewStage;

    @Schema(description = "适用专业方向")
    private String guidanceDirection;

    @Schema(description = "综合意见内容（≤200字）")
    private String opinionContent;

    @Schema(description = "可见范围（DIRECTION-本专业方向）")
    private String targetScope;

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

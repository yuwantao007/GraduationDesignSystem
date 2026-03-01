package com.yuwan.completebackend.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;

/**
 * 课题审查记录VO
 *
 * @author 系统架构师
 * @version 1.0
 * @since 2026-02-23
 */
@Data
@Schema(description = "课题审查记录响应")
public class TopicReviewRecordVO {

    @Schema(description = "审查记录ID")
    private String reviewId;

    @Schema(description = "课题ID")
    private String topicId;

    @Schema(description = "课题名称")
    private String topicTitle;

    @Schema(description = "审查阶段（1-预审 2-初审 3-终审）")
    private Integer reviewStage;

    @Schema(description = "审查阶段名称")
    private String reviewStageName;

    @Schema(description = "审查人ID")
    private String reviewerId;

    @Schema(description = "审查人角色代码")
    private String reviewerRole;

    @Schema(description = "审查人角色名称")
    private String reviewerRoleName;

    @Schema(description = "审查人姓名")
    private String reviewerName;

    @Schema(description = "审查结果（1-通过 2-需修改 3-不通过）")
    private Integer reviewResult;

    @Schema(description = "审查结果名称")
    private String reviewResultName;

    @Schema(description = "审查意见")
    private String reviewOpinion;

    @Schema(description = "是否批量审查")
    private Boolean isBatchReview;

    @Schema(description = "是否被修改过")
    private Boolean isModified;

    @Schema(description = "修改人姓名")
    private String modifiedByName;

    @Schema(description = "修改时间")
    private Date modifiedTime;

    @Schema(description = "审查时间")
    private Date createTime;
}

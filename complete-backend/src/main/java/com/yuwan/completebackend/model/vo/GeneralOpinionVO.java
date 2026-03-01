package com.yuwan.completebackend.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;

/**
 * 综合审查意见VO
 *
 * @author 系统架构师
 * @version 1.0
 * @since 2026-02-23
 */
@Data
@Schema(description = "综合审查意见响应")
public class GeneralOpinionVO {

    @Schema(description = "意见ID")
    private String opinionId;

    @Schema(description = "提交人ID")
    private String reviewerId;

    @Schema(description = "提交人姓名")
    private String reviewerName;

    @Schema(description = "提交人角色代码")
    private String reviewerRole;

    @Schema(description = "提交人角色名称")
    private String reviewerRoleName;

    @Schema(description = "审查阶段（1-预审 2-初审 3-终审）")
    private Integer reviewStage;

    @Schema(description = "审查阶段名称")
    private String reviewStageName;

    @Schema(description = "适用专业方向")
    private String guidanceDirection;

    @Schema(description = "综合意见内容")
    private String opinionContent;

    @Schema(description = "创建时间")
    private Date createTime;
}

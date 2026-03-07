package com.yuwan.completebackend.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 企业信息响应VO
 * 用于返回企业详细信息
 *
 * @author 系统架构师
 * @version 1.0
 * @since 2026-02-21
 */
@Data
@Schema(description = "企业信息响应")
public class EnterpriseVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "企业ID")
    private String enterpriseId;

    @Schema(description = "企业名称")
    private String enterpriseName;

    @Schema(description = "企业编码")
    private String enterpriseCode;

    @Schema(description = "企业负责人用户ID")
    private String leaderId;

    @Schema(description = "企业负责人姓名")
    private String leaderName;

    @Schema(description = "企业负责人手机")
    private String leaderPhone;

    @Schema(description = "企业负责人邮箱")
    private String leaderEmail;

    @Schema(description = "联系人")
    private String contactPerson;

    @Schema(description = "联系电话")
    private String contactPhone;

    @Schema(description = "联系邮箱")
    private String contactEmail;

    @Schema(description = "企业地址")
    private String address;

    @Schema(description = "企业简介")
    private String description;

    @Schema(description = "状态（0-禁用 1-正常）")
    private Integer enterpriseStatus;

    @Schema(description = "状态描述")
    private String enterpriseStatusDesc;

    @Schema(description = "创建时间")
    private String createTime;

    @Schema(description = "更新时间")
    private String updateTime;
}

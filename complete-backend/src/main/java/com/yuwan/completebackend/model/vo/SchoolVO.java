package com.yuwan.completebackend.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 学校信息响应VO
 * 用于返回学校详细信息
 *
 * @author 系统架构师
 * @version 1.0
 * @since 2026-02-22
 */
@Data
@Schema(description = "学校信息响应")
public class SchoolVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "学校ID")
    private String schoolId;

    @Schema(description = "学校名称")
    private String schoolName;

    @Schema(description = "学校编码")
    private String schoolCode;

    @Schema(description = "详细地址")
    private String address;

    @Schema(description = "联系人")
    private String contactPerson;

    @Schema(description = "联系电话")
    private String contactPhone;

    @Schema(description = "学校邮箱")
    private String schoolEmail;

    @Schema(description = "学校简介")
    private String description;

    @Schema(description = "状态（0-禁用 1-正常）")
    private Integer schoolStatus;

    @Schema(description = "状态描述")
    private String schoolStatusDesc;

    @Schema(description = "创建时间")
    private String createTime;

    @Schema(description = "更新时间")
    private String updateTime;
}

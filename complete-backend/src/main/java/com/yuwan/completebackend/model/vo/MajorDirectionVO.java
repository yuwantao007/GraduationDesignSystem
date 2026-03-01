package com.yuwan.completebackend.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 专业方向响应VO
 * 用于返回专业方向详细信息
 *
 * @author 系统架构师
 * @version 1.0
 * @since 2026-03-01
 */
@Data
@Schema(description = "专业方向信息响应")
public class MajorDirectionVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "专业方向ID")
    private String directionId;

    @Schema(description = "所属企业ID")
    private String enterpriseId;

    @Schema(description = "所属企业名称")
    private String enterpriseName;

    @Schema(description = "专业方向名称")
    private String directionName;

    @Schema(description = "专业方向代码")
    private String directionCode;

    @Schema(description = "专业方向描述")
    private String description;

    @Schema(description = "方向负责人ID")
    private String leaderId;

    @Schema(description = "方向负责人姓名")
    private String leaderName;

    @Schema(description = "排序")
    private Integer sortOrder;

    @Schema(description = "状态（1-启用 0-禁用）")
    private Integer status;

    @Schema(description = "状态描述")
    private String statusDesc;

    @Schema(description = "下属专业数量")
    private Integer majorCount;

    @Schema(description = "创建时间")
    private String createTime;

    @Schema(description = "更新时间")
    private String updateTime;
}

package com.yuwan.completebackend.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 专业响应VO
 * 用于返回专业详细信息
 *
 * @author 系统架构师
 * @version 1.0
 * @since 2026-03-01
 */
@Data
@Schema(description = "专业信息响应")
public class MajorVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "专业ID")
    private String majorId;

    @Schema(description = "所属专业方向ID")
    private String directionId;

    @Schema(description = "所属专业方向名称")
    private String directionName;

    @Schema(description = "所属企业ID")
    private String enterpriseId;

    @Schema(description = "所属企业名称")
    private String enterpriseName;

    @Schema(description = "专业名称")
    private String majorName;

    @Schema(description = "专业代码")
    private String majorCode;

    @Schema(description = "学位类型（本科/专科）")
    private String degreeType;

    @Schema(description = "学制（年）")
    private Integer educationYears;

    @Schema(description = "专业描述")
    private String description;

    @Schema(description = "排序")
    private Integer sortOrder;

    @Schema(description = "状态（1-启用 0-禁用）")
    private Integer status;

    @Schema(description = "状态描述")
    private String statusDesc;

    @Schema(description = "创建时间")
    private String createTime;

    @Schema(description = "更新时间")
    private String updateTime;
}

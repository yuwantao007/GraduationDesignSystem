package com.yuwan.completebackend.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 专业树型结构VO
 * 用于返回企业→专业方向→专业的树型结构
 *
 * @author 系统架构师
 * @version 1.0
 * @since 2026-03-01
 */
@Data
@Schema(description = "专业树型结构")
public class MajorTreeVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "节点ID")
    private String id;

    @Schema(description = "节点标签（名称）")
    private String label;

    @Schema(description = "节点类型：enterprise-企业 direction-专业方向 major-专业")
    private String type;

    @Schema(description = "节点代码")
    private String code;

    @Schema(description = "状态（1-启用 0-禁用）")
    private Integer status;

    @Schema(description = "状态描述")
    private String statusDesc;

    @Schema(description = "排序")
    private Integer sortOrder;

    @Schema(description = "描述")
    private String description;

    @Schema(description = "学位类型（专业节点特有）")
    private String degreeType;

    @Schema(description = "学制（专业节点特有）")
    private Integer educationYears;

    @Schema(description = "负责人姓名（方向节点特有）")
    private String leaderName;

    @Schema(description = "子节点列表")
    private List<MajorTreeVO> children;

    @Schema(description = "是否为叶子节点")
    private Boolean isLeaf;
}

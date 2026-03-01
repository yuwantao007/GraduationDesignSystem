package com.yuwan.completebackend.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 企业概览统计VO
 * 用于企业概览页面展示企业的基本信息和统计数据
 *
 * @author 系统架构师
 * @version 1.0
 * @since 2026-03-01
 */
@Data
@Schema(description = "企业概览统计信息")
public class EnterpriseOverviewVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "企业ID")
    private String enterpriseId;

    @Schema(description = "企业名称")
    private String enterpriseName;

    @Schema(description = "企业代码")
    private String enterpriseCode;

    @Schema(description = "企业负责人")
    private String leaderName;

    @Schema(description = "负责人电话")
    private String leaderPhone;

    @Schema(description = "企业地址")
    private String address;

    @Schema(description = "状态（0-禁用 1-启用）")
    private Integer status;

    @Schema(description = "状态描述")
    private String statusDesc;

    @Schema(description = "专业方向数量")
    private Integer directionCount;

    @Schema(description = "专业数量")
    private Integer majorCount;

    @Schema(description = "企业教师数量")
    private Integer teacherCount;

    @Schema(description = "学生数量")
    private Integer studentCount;

    @Schema(description = "创建时间")
    private String createTime;

    @Schema(description = "专业方向列表（包含专业详情）")
    private List<DirectionOverviewVO> directions;
}

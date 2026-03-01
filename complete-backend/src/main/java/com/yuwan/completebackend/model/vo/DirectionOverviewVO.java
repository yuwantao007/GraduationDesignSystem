package com.yuwan.completebackend.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 专业方向概览VO
 * 用于企业概览页面展示专业方向信息
 *
 * @author 系统架构师
 * @version 1.0
 * @since 2026-03-01
 */
@Data
@Schema(description = "专业方向概览VO")
public class DirectionOverviewVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "专业方向ID")
    private String directionId;

    @Schema(description = "专业方向名称")
    private String directionName;

    @Schema(description = "专业方向代码")
    private String directionCode;

    @Schema(description = "方向负责人")
    private String leaderName;

    @Schema(description = "该方向下的教师数量")
    private Integer teacherCount;

    @Schema(description = "该方向下的学生数量")
    private Integer studentCount;

    @Schema(description = "该方向下的专业列表")
    private List<MajorOverviewVO> majors;
}

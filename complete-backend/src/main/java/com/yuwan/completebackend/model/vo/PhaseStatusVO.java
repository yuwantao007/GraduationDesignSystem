package com.yuwan.completebackend.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 阶段状态响应VO
 * 包含当前阶段信息和全部阶段列表
 *
 * @author 系统架构师
 * @version 1.0
 * @since 2026-03-07
 */
@Data
@Schema(description = "阶段状态响应")
public class PhaseStatusVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "当前阶段代码")
    private String phaseCode;

    @Schema(description = "当前阶段中文名")
    private String phaseName;

    @Schema(description = "当前阶段序号")
    private Integer phaseOrder;

    @Schema(description = "总阶段数")
    private Integer totalPhases;

    @Schema(description = "切换时间")
    private String switchTime;

    @Schema(description = "操作人姓名")
    private String operatorName;

    @Schema(description = "毕业届别（如：2026届）")
    private String cohort;

    @Schema(description = "进度百分比（0-100）")
    private Integer progressPercent;

    @Schema(description = "是否已初始化（是否已有阶段记录）")
    private Boolean initialized;

    @Schema(description = "全部阶段列表")
    private List<PhaseItemVO> phaseList;
}

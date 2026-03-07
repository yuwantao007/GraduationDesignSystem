package com.yuwan.completebackend.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 阶段切换记录响应VO
 * 用于展示阶段切换历史
 *
 * @author 系统架构师
 * @version 1.0
 * @since 2026-03-07
 */
@Data
@Schema(description = "阶段切换记录响应")
public class PhaseRecordVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "记录ID")
    private String recordId;

    @Schema(description = "毕业届别")
    private String cohort;

    @Schema(description = "阶段代码")
    private String phaseCode;

    @Schema(description = "阶段中文名")
    private String phaseName;

    @Schema(description = "阶段序号")
    private Integer phaseOrder;

    @Schema(description = "前一阶段代码")
    private String previousPhaseCode;

    @Schema(description = "前一阶段中文名")
    private String previousPhaseName;

    @Schema(description = "切换时间")
    private String switchTime;

    @Schema(description = "操作人ID")
    private String operatorId;

    @Schema(description = "操作人姓名")
    private String operatorName;

    @Schema(description = "切换原因")
    private String switchReason;

    @Schema(description = "是否为当前生效记录")
    private Boolean isCurrent;
}

package com.yuwan.completebackend.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 阶段项VO
 * 用于阶段列表中的单个阶段展示
 *
 * @author 系统架构师
 * @version 1.0
 * @since 2026-03-07
 */
@Data
@Schema(description = "阶段项信息")
public class PhaseItemVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "阶段代码")
    private String phaseCode;

    @Schema(description = "阶段中文名")
    private String phaseName;

    @Schema(description = "阶段序号")
    private Integer order;

    @Schema(description = "阶段状态（COMPLETED-已完成 ACTIVE-进行中 PENDING-未开始）")
    private String status;

    @Schema(description = "切换时间（未开始则为null）")
    private String switchTime;

    @Schema(description = "阶段描述")
    private String description;

    @Schema(description = "前端展示图标")
    private String icon;

    @Schema(description = "前端展示颜色")
    private String color;
}

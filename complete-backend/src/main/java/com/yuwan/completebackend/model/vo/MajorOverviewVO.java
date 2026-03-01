package com.yuwan.completebackend.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 专业概览VO
 * 用于企业概览页面展示专业信息
 *
 * @author 系统架构师
 * @version 1.0
 * @since 2026-03-01
 */
@Data
@Schema(description = "专业概览VO")
public class MajorOverviewVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "专业ID")
    private String majorId;

    @Schema(description = "专业名称")
    private String majorName;

    @Schema(description = "专业代码")
    private String majorCode;

    @Schema(description = "学位类型")
    private String degreeType;
}

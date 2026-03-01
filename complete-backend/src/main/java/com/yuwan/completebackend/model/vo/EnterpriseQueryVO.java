package com.yuwan.completebackend.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 企业查询参数VO
 * 用于接收企业列表查询条件
 *
 * @author 系统架构师
 * @version 1.0
 * @since 2026-02-21
 */
@Data
@Schema(description = "企业查询参数")
public class EnterpriseQueryVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "企业名称（模糊查询）")
    private String enterpriseName;

    @Schema(description = "企业编码")
    private String enterpriseCode;

    @Schema(description = "关键词（企业名称或编码，用于概览页面）")
    private String keyword;

    @Schema(description = "状态（0-禁用 1-正常）")
    private Integer enterpriseStatus;

    @Schema(description = "状态（用于概览页面，0-禁用 1-正常）")
    private Integer status;

    @Schema(description = "页码", example = "1")
    private Integer pageNum = 1;

    @Schema(description = "每页大小", example = "10")
    private Integer pageSize = 10;
}

package com.yuwan.completebackend.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 专业查询参数VO
 * 用于接收分页查询条件
 *
 * @author 系统架构师
 * @version 1.0
 * @since 2026-03-01
 */
@Data
@Schema(description = "专业查询参数")
public class MajorQueryVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "企业ID（可选，系统管理员可指定）")
    private String enterpriseId;

    @Schema(description = "专业方向ID（可选）")
    private String directionId;

    @Schema(description = "名称关键字（模糊查询）")
    private String keyword;

    @Schema(description = "状态筛选（0-禁用 1-启用）")
    private Integer status;

    @Schema(description = "页码", defaultValue = "1")
    private Integer pageNum = 1;

    @Schema(description = "每页大小", defaultValue = "10")
    private Integer pageSize = 10;
}

package com.yuwan.completebackend.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 学校查询参数VO
 * 用于接收学校列表查询条件
 *
 * @author 系统架构师
 * @version 1.0
 * @since 2026-02-22
 */
@Data
@Schema(description = "学校查询参数")
public class SchoolQueryVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "学校名称（模糊查询）")
    private String schoolName;

    @Schema(description = "学校编码")
    private String schoolCode;

    @Schema(description = "状态（0-禁用 1-正常）")
    private Integer schoolStatus;

    @Schema(description = "页码", defaultValue = "1")
    private Integer pageNum = 1;

    @Schema(description = "每页大小", defaultValue = "10")
    private Integer pageSize = 10;
}

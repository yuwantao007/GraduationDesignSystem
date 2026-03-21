package com.yuwan.completebackend.model.dto.defense;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 开题报告查询DTO
 *
 * @author 系统架构师
 * @version 1.0
 * @since 2026-03-17
 */
@Data
@Schema(description = "开题报告查询请求对象")
public class ReportQueryDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "学生姓名（模糊查询）")
    private String studentName;

    @Schema(description = "状态: 0=草稿, 1=已定稿")
    private Integer status;

    @Schema(description = "答辩安排ID")
    private String arrangementId;

    @Schema(description = "当前页码", example = "1")
    private Integer pageNum = 1;

    @Schema(description = "每页大小", example = "10")
    private Integer pageSize = 10;
}

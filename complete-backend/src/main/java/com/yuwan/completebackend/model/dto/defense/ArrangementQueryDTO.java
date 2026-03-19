package com.yuwan.completebackend.model.dto.defense;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 答辩安排查询DTO
 *
 * @author 系统架构师
 * @version 1.0
 * @since 2026-03-17
 */
@Data
@Schema(description = "答辩安排查询请求对象")
public class ArrangementQueryDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "答辩类型: 1=开题, 2=中期, 3=正式, 4=二次")
    private Integer defenseType;

    @Schema(description = "课题类别")
    private String topicCategory;

    @Schema(description = "专业ID")
    private String majorId;

    @Schema(description = "毕业届别")
    private String cohort;

    @Schema(description = "状态: 1=启用, 0=禁用")
    private Integer status;

    @Schema(description = "当前页码", example = "1")
    private Integer pageNum = 1;

    @Schema(description = "每页大小", example = "10")
    private Integer pageSize = 10;
}

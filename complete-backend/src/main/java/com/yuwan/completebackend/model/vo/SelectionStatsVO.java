package com.yuwan.completebackend.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 课题选报统计 VO（漏斗图数据）
 * <p>描述选报流程各环节的数量漏斗：总选报 → 确认中 → 中选</p>
 *
 * @author 系统架构师
 * @version 1.0
 * @since 2026-03-14
 */
@Data
@Schema(description = "课题选报漏斗统计数据")
public class SelectionStatsVO {

    @Schema(description = "总选报记录数（含待确认/中选/落选）")
    private long totalSelections;

    @Schema(description = "待确认数量（status=0）")
    private long pendingCount;

    @Schema(description = "中选数量（status=1）")
    private long selectedCount;

    @Schema(description = "落选数量（status=2）")
    private long rejectedCount;

    @Schema(description = "中选率（%）= 中选数 / 总选报数 * 100")
    private double confirmRate;

    @Schema(description = "已选报学生数（至少有一条选报记录）")
    private long studentsWithSelection;

    @Schema(description = "学生总数")
    private long totalStudents;

    @Schema(description = "学生选报覆盖率（%）= 已选报学生数 / 学生总数 * 100")
    private double studentCoverageRate;
}

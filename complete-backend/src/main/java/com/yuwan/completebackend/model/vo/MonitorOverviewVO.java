package com.yuwan.completebackend.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 质量监控总览 VO
 * <p>提供首页数字卡片所需的顶层汇总统计数据</p>
 *
 * @author 系统架构师
 * @version 1.0
 * @since 2026-03-14
 */
@Data
@Schema(description = "质量监控总览数据")
public class MonitorOverviewVO {

    @Schema(description = "课题总数")
    private long totalTopics;

    @Schema(description = "已通过终审的课题数")
    private long approvedTopics;

    @Schema(description = "学生总数")
    private long totalStudents;

    @Schema(description = "中选学生数（已确认选报）")
    private long selectedStudents;

    @Schema(description = "选报率（%）= 中选学生数 / 学生总数 * 100，保留一位小数")
    private double selectionRate;

    @Schema(description = "未读预警数")
    private long unreadAlerts;

    @Schema(description = "当前阶段名称")
    private String currentPhaseName;

    @Schema(description = "当前毕业届别")
    private String currentCohort;

    @Schema(description = "当前整体进度（%）")
    private int overallProgress;
}

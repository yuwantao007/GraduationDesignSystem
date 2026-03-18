package com.yuwan.completebackend.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 首页四项统计数据 VO
 */
@Data
@Schema(description = "首页四项统计数据")
public class DashboardStatsVO {

    @Schema(description = "总用户数")
    private long totalUsers;

    @Schema(description = "课题总数")
    private long totalTopics;

    @Schema(description = "待审审批数（当前按待预审课题统计）")
    private long pendingApprovals;

    @Schema(description = "今日访问数（文档预览+下载访问日志）")
    private long todayVisits;
}

package com.yuwan.completebackend.controller;

import com.yuwan.completebackend.common.Result;
import com.yuwan.completebackend.model.vo.MonitorOverviewVO;
import com.yuwan.completebackend.model.vo.SelectionStatsVO;
import com.yuwan.completebackend.model.vo.TopicStatusDistVO;
import com.yuwan.completebackend.service.IMonitorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 质量监控控制器
 * <p>提供仪表盘所需的统计数据接口，全部为只读操作</p>
 *
 * @author 系统架构师
 * @version 1.0
 * @since 2026-03-14
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/monitor")
@Tag(name = "质量监控", description = "质量监控仪表盘统计数据接口")
public class MonitorController {

    private final IMonitorService monitorService;

    /**
     * 获取监控总览数据（数字卡片）
     *
     * @return 课题总数/中选学生数/未读预警数/当前阶段信息
     */
    @GetMapping("/overview")
    @PreAuthorize("hasAuthority('monitor:dashboard:view')")
    @Operation(summary = "监控总览", description = "返回课题总数、学生中选数、选报率、未读预警数、当前阶段等顶层汇总指标")
    public Result<MonitorOverviewVO> getOverview() {
        return Result.success(monitorService.getOverview());
    }

    /**
     * 获取课题审查状态分布（饼图数据）
     *
     * @return 各审查状态对应的课题数量列表
     */
    @GetMapping("/topic/status")
    @PreAuthorize("hasAuthority('monitor:dashboard:view')")
    @Operation(summary = "课题状态分布", description = "返回各审查状态（草稿/待预审/预审通过等）的课题数量，用于饼图展示")
    public Result<List<TopicStatusDistVO>> getTopicStatusDist() {
        return Result.success(monitorService.getTopicStatusDist());
    }

    /**
     * 获取各企业课题数量（横向柱状图数据）
     *
     * @return 前10名企业课题数量列表
     */
    @GetMapping("/topic/enterprise")
    @PreAuthorize("hasAuthority('monitor:dashboard:view')")
    @Operation(summary = "企业课题分布", description = "返回课题数量最多的前10家企业，用于横向柱状图展示")
    public Result<List<Map<String, Object>>> getTopicCountByEnterprise() {
        return Result.success(monitorService.getTopicCountByEnterprise());
    }

    /**
     * 获取选报漏斗统计数据
     *
     * @return 总选报/待确认/中选/落选数量及各比率
     */
    @GetMapping("/selection/stats")
    @PreAuthorize("hasAuthority('monitor:dashboard:view')")
    @Operation(summary = "选报漏斗统计", description = "返回选报总数、各状态数量及中选率、学生覆盖率，用于漏斗图和圆环图")
    public Result<SelectionStatsVO> getSelectionStats() {
        return Result.success(monitorService.getSelectionStats());
    }
}

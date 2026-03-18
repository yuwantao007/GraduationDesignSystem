package com.yuwan.completebackend.controller;

import com.yuwan.completebackend.common.Result;
import com.yuwan.completebackend.model.vo.DashboardStatsVO;
import com.yuwan.completebackend.service.IDashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 首页统计控制器
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/dashboard")
@Tag(name = "首页统计", description = "首页四项统计数据接口")
public class DashboardController {

    private final IDashboardService dashboardService;

    @GetMapping("/stats")
    @Operation(summary = "获取首页统计", description = "返回总用户数、课题总数、待审审批、今日访问")
    public Result<DashboardStatsVO> getDashboardStats() {
        return Result.success(dashboardService.getDashboardStats());
    }
}

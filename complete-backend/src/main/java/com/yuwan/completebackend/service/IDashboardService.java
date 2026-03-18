package com.yuwan.completebackend.service;

import com.yuwan.completebackend.model.vo.DashboardStatsVO;

/**
 * 首页统计服务接口
 */
public interface IDashboardService {

    /**
     * 获取首页四项统计数据
     */
    DashboardStatsVO getDashboardStats();
}

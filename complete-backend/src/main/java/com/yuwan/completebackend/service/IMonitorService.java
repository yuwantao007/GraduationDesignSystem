package com.yuwan.completebackend.service;

import com.yuwan.completebackend.model.vo.MonitorOverviewVO;
import com.yuwan.completebackend.model.vo.SelectionStatsVO;
import com.yuwan.completebackend.model.vo.TopicStatusDistVO;

import java.util.List;
import java.util.Map;

/**
 * 质量监控服务接口
 * <p>提供质量监控仪表盘所需的各类统计数据查询功能</p>
 *
 * @author 系统架构师
 * @version 1.0
 * @since 2026-03-14
 */
public interface IMonitorService {

    /**
     * 获取监控总览数据（首页数字卡片）
     *
     * @return 总览统计数据
     */
    MonitorOverviewVO getOverview();

    /**
     * 获取课题审查状态分布（饼图数据）
     *
     * @return 各状态对应的课题数量列表
     */
    List<TopicStatusDistVO> getTopicStatusDist();

    /**
     * 获取各企业课题数量统计（横向柱状图数据）
     * 取数量最多的前10家企业
     *
     * @return key=企业名称，value=课题数量
     */
    List<Map<String, Object>> getTopicCountByEnterprise();

    /**
     * 获取选报漏斗统计数据
     *
     * @return 选报各环节数量与比率
     */
    SelectionStatsVO getSelectionStats();
}

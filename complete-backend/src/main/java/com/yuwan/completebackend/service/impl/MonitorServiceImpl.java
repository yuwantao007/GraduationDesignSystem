package com.yuwan.completebackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yuwan.completebackend.mapper.AlertMapper;
import com.yuwan.completebackend.mapper.TopicMapper;
import com.yuwan.completebackend.mapper.TopicSelectionMapper;
import com.yuwan.completebackend.mapper.UserMapper;
import com.yuwan.completebackend.model.entity.Alert;
import com.yuwan.completebackend.model.entity.Topic;
import com.yuwan.completebackend.model.entity.TopicSelection;
import com.yuwan.completebackend.model.vo.MonitorOverviewVO;
import com.yuwan.completebackend.model.vo.PhaseStatusVO;
import com.yuwan.completebackend.model.vo.SelectionStatsVO;
import com.yuwan.completebackend.model.vo.TopicStatusDistVO;
import com.yuwan.completebackend.service.IMonitorService;
import com.yuwan.completebackend.service.ISystemPhaseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 质量监控服务实现类
 * <p>
 * 所有查询均为只读操作，直接复用已有 Mapper，
 * 不修改任何业务数据，对现有功能模块零影响。
 * </p>
 *
 * @author 系统架构师
 * @version 1.0
 * @since 2026-03-14
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MonitorServiceImpl implements IMonitorService {

    private final TopicMapper topicMapper;
    private final TopicSelectionMapper topicSelectionMapper;
    private final UserMapper userMapper;
    private final AlertMapper alertMapper;
    private final ISystemPhaseService systemPhaseService;

    // 课题审查状态名称映射
    private static final String[] REVIEW_STATUS_NAMES = {
            "草稿", "待预审", "预审通过", "预审需修改",
            "初审通过", "初审需修改", "终审通过", "终审不通过"
    };

    @Override
    public MonitorOverviewVO getOverview() {
        MonitorOverviewVO vo = new MonitorOverviewVO();

        // 课题总数
        long totalTopics = topicMapper.selectCount(
                new LambdaQueryWrapper<Topic>().eq(Topic::getDeleted, 0));
        vo.setTotalTopics(totalTopics);

        // 已通过终审的课题数（reviewStatus=6）
        long approvedTopics = topicMapper.selectCount(
                new LambdaQueryWrapper<Topic>()
                        .eq(Topic::getReviewStatus, 6)
                        .eq(Topic::getDeleted, 0));
        vo.setApprovedTopics(approvedTopics);

        // 学生总数（role_code=STUDENT）
        long totalStudents = userMapper.countByRoleCode("STUDENT");
        vo.setTotalStudents(totalStudents);

        // 中选学生数（selectionStatus=1 且 deleted=0）
        long selectedStudents = topicSelectionMapper.selectCount(
                new LambdaQueryWrapper<TopicSelection>()
                        .eq(TopicSelection::getSelectionStatus, 1)
                        .eq(TopicSelection::getDeleted, 0));
        vo.setSelectedStudents(selectedStudents);

        // 选报率
        double selectionRate = 0.0;
        if (totalStudents > 0) {
            selectionRate = BigDecimal.valueOf(selectedStudents * 100.0 / totalStudents)
                    .setScale(1, RoundingMode.HALF_UP).doubleValue();
        }
        vo.setSelectionRate(selectionRate);

        // 未读预警数
        long unreadAlerts = alertMapper.selectCount(
                new LambdaQueryWrapper<Alert>()
                        .eq(Alert::getIsRead, 0)
                        .eq(Alert::getDeleted, 0));
        vo.setUnreadAlerts(unreadAlerts);

        // 当前阶段信息（安全获取，阶段未初始化时不报错）
        try {
            PhaseStatusVO phaseStatus = systemPhaseService.getCurrentPhaseStatus();
            if (phaseStatus != null && Boolean.TRUE.equals(phaseStatus.getInitialized())) {
                vo.setCurrentPhaseName(phaseStatus.getPhaseName());
                vo.setCurrentCohort(phaseStatus.getCohort());
                vo.setOverallProgress(phaseStatus.getProgressPercent() != null
                        ? phaseStatus.getProgressPercent() : 0);
            } else {
                vo.setCurrentPhaseName("未初始化");
                vo.setCurrentCohort("-");
                vo.setOverallProgress(0);
            }
        } catch (Exception e) {
            log.warn("获取当前阶段状态失败，使用默认值: {}", e.getMessage());
            vo.setCurrentPhaseName("未知");
            vo.setCurrentCohort("-");
            vo.setOverallProgress(0);
        }

        return vo;
    }

    @Override
    public List<TopicStatusDistVO> getTopicStatusDist() {
        List<TopicStatusDistVO> result = new ArrayList<>();

        for (int statusCode = 0; statusCode <= 7; statusCode++) {
            final int code = statusCode;
            long count = topicMapper.selectCount(
                    new LambdaQueryWrapper<Topic>()
                            .eq(Topic::getReviewStatus, code)
                            .eq(Topic::getDeleted, 0));
            if (count > 0) {
                result.add(new TopicStatusDistVO(code, REVIEW_STATUS_NAMES[code], count));
            }
        }

        return result;
    }

    @Override
    public List<Map<String, Object>> getTopicCountByEnterprise() {
        return alertMapper.selectTopicCountByEnterprise();
    }

    @Override
    public SelectionStatsVO getSelectionStats() {
        SelectionStatsVO vo = new SelectionStatsVO();

        // 总选报数
        long total = topicSelectionMapper.selectCount(
                new LambdaQueryWrapper<TopicSelection>().eq(TopicSelection::getDeleted, 0));
        vo.setTotalSelections(total);

        // 待确认
        long pending = topicSelectionMapper.selectCount(
                new LambdaQueryWrapper<TopicSelection>()
                        .eq(TopicSelection::getSelectionStatus, 0)
                        .eq(TopicSelection::getDeleted, 0));
        vo.setPendingCount(pending);

        // 中选
        long selected = topicSelectionMapper.selectCount(
                new LambdaQueryWrapper<TopicSelection>()
                        .eq(TopicSelection::getSelectionStatus, 1)
                        .eq(TopicSelection::getDeleted, 0));
        vo.setSelectedCount(selected);

        // 落选
        long rejected = topicSelectionMapper.selectCount(
                new LambdaQueryWrapper<TopicSelection>()
                        .eq(TopicSelection::getSelectionStatus, 2)
                        .eq(TopicSelection::getDeleted, 0));
        vo.setRejectedCount(rejected);

        // 中选率
        double confirmRate = 0.0;
        if (total > 0) {
            confirmRate = BigDecimal.valueOf(selected * 100.0 / total)
                    .setScale(1, RoundingMode.HALF_UP).doubleValue();
        }
        vo.setConfirmRate(confirmRate);

        // 已选报学生数（去重，至少有一条非删除的选报记录）
        long studentsWithSelection = topicSelectionMapper.countDistinctStudentsWithSelection();
        vo.setStudentsWithSelection(studentsWithSelection);

        // 学生总数（role_code=STUDENT）
        long totalStudents = userMapper.countByRoleCode("STUDENT");
        vo.setTotalStudents(totalStudents);

        // 学生覆盖率
        double coverageRate = 0.0;
        if (totalStudents > 0) {
            coverageRate = BigDecimal.valueOf(studentsWithSelection * 100.0 / totalStudents)
                    .setScale(1, RoundingMode.HALF_UP).doubleValue();
        }
        vo.setStudentCoverageRate(coverageRate);

        return vo;
    }
}

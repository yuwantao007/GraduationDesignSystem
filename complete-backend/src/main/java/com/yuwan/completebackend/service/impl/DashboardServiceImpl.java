package com.yuwan.completebackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yuwan.completebackend.mapper.DocumentAccessLogMapper;
import com.yuwan.completebackend.mapper.TopicMapper;
import com.yuwan.completebackend.mapper.UserMapper;
import com.yuwan.completebackend.model.entity.DocumentAccessLog;
import com.yuwan.completebackend.model.entity.Topic;
import com.yuwan.completebackend.model.entity.User;
import com.yuwan.completebackend.model.vo.DashboardStatsVO;
import com.yuwan.completebackend.service.IDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

/**
 * 首页统计服务实现
 */
@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements IDashboardService {

    private final UserMapper userMapper;
    private final TopicMapper topicMapper;
    private final DocumentAccessLogMapper documentAccessLogMapper;

    @Override
    public DashboardStatsVO getDashboardStats() {
        DashboardStatsVO stats = new DashboardStatsVO();

        long totalUsers = userMapper.selectCount(
                new LambdaQueryWrapper<User>().eq(User::getDeleted, 0)
        );
        stats.setTotalUsers(totalUsers);

        long totalTopics = topicMapper.selectCount(
                new LambdaQueryWrapper<Topic>().eq(Topic::getDeleted, 0)
        );
        stats.setTotalTopics(totalTopics);

        // 待审审批：按课题待预审（reviewStatus=1）统计
        long pendingApprovals = topicMapper.selectCount(
                new LambdaQueryWrapper<Topic>()
                        .eq(Topic::getReviewStatus, 1)
                        .eq(Topic::getDeleted, 0)
        );
        stats.setPendingApprovals(pendingApprovals);

        LocalDate today = LocalDate.now();
        Date start = Date.from(today.atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date end = Date.from(today.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant());

        long todayVisits = documentAccessLogMapper.selectCount(
                new LambdaQueryWrapper<DocumentAccessLog>()
                        .ge(DocumentAccessLog::getAccessTime, start)
                        .lt(DocumentAccessLog::getAccessTime, end)
        );
        stats.setTodayVisits(todayVisits);

        return stats;
    }
}

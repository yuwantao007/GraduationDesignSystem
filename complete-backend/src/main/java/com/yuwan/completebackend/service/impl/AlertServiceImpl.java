package com.yuwan.completebackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yuwan.completebackend.common.PageResult;
import com.yuwan.completebackend.exception.BusinessException;
import com.yuwan.completebackend.mapper.AlertMapper;
import com.yuwan.completebackend.mapper.SystemPhaseRecordMapper;
import com.yuwan.completebackend.mapper.TopicMapper;
import com.yuwan.completebackend.mapper.TopicSelectionMapper;
import com.yuwan.completebackend.mapper.UserMapper;
import com.yuwan.completebackend.model.entity.Alert;
import com.yuwan.completebackend.model.entity.SystemPhaseRecord;
import com.yuwan.completebackend.model.entity.Topic;
import com.yuwan.completebackend.model.entity.TopicSelection;
import com.yuwan.completebackend.model.enums.AlertTypeEnum;
import com.yuwan.completebackend.model.vo.AlertVO;
import com.yuwan.completebackend.service.IAlertService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 系统预警服务实现类
 * <p>
 * 包含两类方法：<br/>
 * 1. 预警记录管理（查询、标记已读、处理）<br/>
 * 2. 定时检测（由 AlertScheduler 每日调用），仅读取现有业务数据，不修改业务逻辑
 * </p>
 *
 * @author 系统架构师
 * @version 1.0
 * @since 2026-03-14
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AlertServiceImpl implements IAlertService {

    /** 待预审课题积压阈值：超过20个触发警告 */
    private static final int REVIEW_BACKLOG_THRESHOLD = 20;

    /** 阶段截止临近预警天数：≤7天触发 */
    private static final int DEADLINE_WARN_DAYS = 7;

    /** 选报率偏低阈值（%）：< 60% 触发 */
    private static final double SELECTION_RATE_LOW_THRESHOLD = 60.0;

    private final AlertMapper alertMapper;
    private final TopicMapper topicMapper;
    private final TopicSelectionMapper topicSelectionMapper;
    private final UserMapper userMapper;
    private final SystemPhaseRecordMapper systemPhaseRecordMapper;

    // ========================== 预警记录管理 ==========================

    @Override
    public PageResult<AlertVO> listAlerts(String alertType, Integer alertLevel,
                                          Integer isRead, Integer isResolved,
                                          int page, int size) {
        Page<AlertVO> pageParam = new Page<>(page, size);
        IPage<AlertVO> resultPage = alertMapper.selectAlertPage(
                pageParam, alertType, alertLevel, isRead, isResolved);

        // 填充类型与级别描述
        List<AlertVO> records = resultPage.getRecords().stream().map(vo -> {
            fillAlertDesc(vo);
            return vo;
        }).collect(Collectors.toList());

        PageResult<AlertVO> result = new PageResult<>();
        result.setRecords(records);
        result.setTotal(resultPage.getTotal());
        result.setCurrent(resultPage.getCurrent());
        result.setSize(resultPage.getSize());
        return result;
    }

    @Override
    public long getUnreadCount() {
        return alertMapper.selectCount(
                new LambdaQueryWrapper<Alert>()
                        .eq(Alert::getIsRead, 0)
                        .eq(Alert::getDeleted, 0));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markAsRead(String alertId) {
        Alert alert = alertMapper.selectById(alertId);
        if (alert == null || alert.getDeleted() == 1) {
            throw new BusinessException(404, "预警记录不存在");
        }
        alertMapper.update(null,
                new LambdaUpdateWrapper<Alert>()
                        .eq(Alert::getAlertId, alertId)
                        .set(Alert::getIsRead, 1));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markAsResolved(String alertId) {
        Alert alert = alertMapper.selectById(alertId);
        if (alert == null || alert.getDeleted() == 1) {
            throw new BusinessException(404, "预警记录不存在");
        }
        alertMapper.update(null,
                new LambdaUpdateWrapper<Alert>()
                        .eq(Alert::getAlertId, alertId)
                        .set(Alert::getIsResolved, 1)
                        .set(Alert::getResolvedAt, new Date())
                        .set(Alert::getIsRead, 1));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markAllAsRead() {
        alertMapper.update(null,
                new LambdaUpdateWrapper<Alert>()
                        .eq(Alert::getIsRead, 0)
                        .eq(Alert::getDeleted, 0)
                        .set(Alert::getIsRead, 1));
    }

    // ========================== 定时检测方法 ==========================

    @Override
    public void checkStudentNotSelected() {
        // 统计学生总数
        long totalStudents = userMapper.countByRoleCode("STUDENT");
        if (totalStudents == 0) {
            return;
        }

        // 统计已有选报记录的学生数
        long studentsWithSelection = topicSelectionMapper.selectCount(
                new LambdaQueryWrapper<TopicSelection>()
                        .eq(TopicSelection::getDeleted, 0)
                        .groupBy(TopicSelection::getStudentId));

        long notSelected = totalStudents - studentsWithSelection;
        if (notSelected <= 0) {
            return;
        }

        // 避免重复产生同类型预警（当天已有则跳过）
        if (hasTodayAlert(AlertTypeEnum.STUDENT_NOT_SELECTED.getCode())) {
            return;
        }

        Alert alert = buildAlert(
                AlertTypeEnum.STUDENT_NOT_SELECTED.getCode(),
                2,
                "学生未选报课题预警",
                String.format("当前有 %d 名学生尚未选报任何课题（共 %d 名学生），请关注。", notSelected, totalStudents),
                null,
                "STUDENT"
        );
        alertMapper.insert(alert);
        log.info("[预警] 学生未选报课题：{}/{}", notSelected, totalStudents);
    }

    @Override
    public void checkTopicNoApplicant() {
        // 查询已通过终审但无选报记录的课题数量
        long approvedTopics = topicMapper.selectCount(
                new LambdaQueryWrapper<Topic>()
                        .eq(Topic::getReviewStatus, 6)
                        .eq(Topic::getDeleted, 0));

        long topicsWithSelection = topicSelectionMapper.selectCount(
                new LambdaQueryWrapper<TopicSelection>()
                        .eq(TopicSelection::getDeleted, 0)
                        .groupBy(TopicSelection::getTopicId));

        long noApplicantCount = approvedTopics - topicsWithSelection;
        if (noApplicantCount <= 0) {
            return;
        }

        if (hasTodayAlert(AlertTypeEnum.TOPIC_NO_APPLICANT.getCode())) {
            return;
        }

        Alert alert = buildAlert(
                AlertTypeEnum.TOPIC_NO_APPLICANT.getCode(),
                2,
                "课题无人选报预警",
                String.format("当前有 %d 个已通过终审的课题尚无任何学生选报，请关注。", noApplicantCount),
                null,
                "TOPIC"
        );
        alertMapper.insert(alert);
        log.info("[预警] 课题无人选报：{} 个", noApplicantCount);
    }

    @Override
    public void checkReviewBacklog() {
        // 统计待预审（reviewStatus=1）的课题数量
        long pendingReview = topicMapper.selectCount(
                new LambdaQueryWrapper<Topic>()
                        .eq(Topic::getReviewStatus, 1)
                        .eq(Topic::getDeleted, 0));

        if (pendingReview < REVIEW_BACKLOG_THRESHOLD) {
            return;
        }

        if (hasTodayAlert(AlertTypeEnum.REVIEW_BACKLOG.getCode())) {
            return;
        }

        int level = pendingReview >= REVIEW_BACKLOG_THRESHOLD * 2 ? 3 : 2;
        Alert alert = buildAlert(
                AlertTypeEnum.REVIEW_BACKLOG.getCode(),
                level,
                "课题审查积压预警",
                String.format("当前待预审课题数量达到 %d 个（阈值 %d 个），请及时处理。",
                        pendingReview, REVIEW_BACKLOG_THRESHOLD),
                null,
                "PHASE"
        );
        alertMapper.insert(alert);
        log.info("[预警] 课题审查积压：{} 个", pendingReview);
    }

    @Override
    public void checkPhaseDeadline() {
        // 获取当前生效的阶段切换记录
        SystemPhaseRecord currentRecord = systemPhaseRecordMapper.selectOne(
                new LambdaQueryWrapper<SystemPhaseRecord>()
                        .eq(SystemPhaseRecord::getIsCurrent, 1)
                        .last("LIMIT 1"));

        if (currentRecord == null || currentRecord.getSwitchTime() == null) {
            return;
        }

        // 系统阶段无显式截止日期，此处以切换时间 + 90天作为当前阶段预计持续时间估算
        // 生产环境可在 system_phase_config 中扩展 expected_days 字段替代此逻辑
        Calendar deadline = Calendar.getInstance();
        deadline.setTime(currentRecord.getSwitchTime());
        deadline.add(Calendar.DAY_OF_YEAR, 90);

        long diffMs = deadline.getTimeInMillis() - System.currentTimeMillis();
        long diffDays = TimeUnit.MILLISECONDS.toDays(diffMs);

        if (diffDays > DEADLINE_WARN_DAYS || diffDays < 0) {
            return;
        }

        if (hasTodayAlert(AlertTypeEnum.PHASE_DEADLINE_NEAR.getCode())) {
            return;
        }

        Alert alert = buildAlert(
                AlertTypeEnum.PHASE_DEADLINE_NEAR.getCode(),
                3,
                "阶段截止临近预警",
                String.format("当前阶段（%s）距预计截止日期仅剩 %d 天，请及时处理相关事务。",
                        currentRecord.getPhaseCode(), diffDays),
                currentRecord.getPhaseCode(),
                "PHASE"
        );
        alertMapper.insert(alert);
        log.info("[预警] 阶段截止临近：{} 天后", diffDays);
    }

    @Override
    public void checkSelectionRateLow() {
        long totalStudents = userMapper.countByRoleCode("STUDENT");
        if (totalStudents == 0) {
            return;
        }

        long selectedStudents = topicSelectionMapper.selectCount(
                new LambdaQueryWrapper<TopicSelection>()
                        .eq(TopicSelection::getSelectionStatus, 1)
                        .eq(TopicSelection::getDeleted, 0));

        double rate = BigDecimal.valueOf(selectedStudents * 100.0 / totalStudents)
                .setScale(1, RoundingMode.HALF_UP).doubleValue();

        if (rate >= SELECTION_RATE_LOW_THRESHOLD) {
            return;
        }

        if (hasTodayAlert(AlertTypeEnum.SELECTION_RATE_LOW.getCode())) {
            return;
        }

        Alert alert = buildAlert(
                AlertTypeEnum.SELECTION_RATE_LOW.getCode(),
                2,
                "选报率偏低预警",
                String.format("当前中选率为 %.1f%%（阈值 %.0f%%），中选学生 %d / %d 人，请关注选报进展。",
                        rate, SELECTION_RATE_LOW_THRESHOLD, selectedStudents, totalStudents),
                null,
                "STUDENT"
        );
        alertMapper.insert(alert);
        log.info("[预警] 选报率偏低：{}%", rate);
    }

    // ========================== 私有工具方法 ==========================

    /**
     * 检查当天是否已存在同类型预警（避免重复产生）
     */
    private boolean hasTodayAlert(String alertType) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        Date startOfDay = cal.getTime();

        long count = alertMapper.selectCount(
                new LambdaQueryWrapper<Alert>()
                        .eq(Alert::getAlertType, alertType)
                        .ge(Alert::getCreateTime, startOfDay)
                        .eq(Alert::getDeleted, 0));
        return count > 0;
    }

    /**
     * 构建预警实体
     */
    private Alert buildAlert(String alertType, int alertLevel,
                              String title, String content,
                              String targetId, String targetType) {
        Alert alert = new Alert();
        alert.setAlertType(alertType);
        alert.setAlertLevel(alertLevel);
        alert.setAlertTitle(title);
        alert.setAlertContent(content);
        alert.setTargetId(targetId);
        alert.setTargetType(targetType);
        alert.setIsRead(0);
        alert.setIsResolved(0);
        alert.setDeleted(0);
        return alert;
    }

    /**
     * 填充预警 VO 的类型描述和级别描述
     */
    private void fillAlertDesc(AlertVO vo) {
        // 类型描述
        AlertTypeEnum typeEnum = AlertTypeEnum.fromCode(vo.getAlertType());
        vo.setAlertTypeDesc(typeEnum != null ? typeEnum.getDescription() : vo.getAlertType());

        // 级别描述
        String levelDesc = switch (vo.getAlertLevel() == null ? 1 : vo.getAlertLevel()) {
            case 1 -> "提示";
            case 2 -> "警告";
            case 3 -> "严重";
            default -> "未知";
        };
        vo.setAlertLevelDesc(levelDesc);
    }
}

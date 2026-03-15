package com.yuwan.completebackend.scheduler;

import com.yuwan.completebackend.service.IAlertService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 智能预警定时任务调度器
 * <p>
 * 每天 08:00 执行一次全量预警检测，仅读取业务数据，
 * 不修改任何已有业务逻辑，对现有功能模块零影响。
 * </p>
 *
 * @author 系统架构师
 * @version 1.0
 * @since 2026-03-14
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AlertScheduler {

    private final IAlertService alertService;

    /**
     * 每日 08:00 执行智能预警检测
     * <p>按顺序执行各类预警规则检测，任意一项异常不影响其他规则执行</p>
     */
    @Scheduled(cron = "0 0 8 * * ?")
    public void scheduledAlertCheck() {
        log.info("[智能预警] 开始执行每日预警检测...");

        runSafely("学生未选报检测", alertService::checkStudentNotSelected);
        runSafely("课题无人选报检测", alertService::checkTopicNoApplicant);
        runSafely("课题审查积压检测", alertService::checkReviewBacklog);
        runSafely("阶段截止临近检测", alertService::checkPhaseDeadline);
        runSafely("选报率偏低检测", alertService::checkSelectionRateLow);

        log.info("[智能预警] 每日预警检测完成。");
    }

    /**
     * 安全执行预警检测任务，捕获异常避免单项失败影响整体调度
     *
     * @param taskName 任务名称（仅用于日志）
     * @param task     检测任务
     */
    private void runSafely(String taskName, Runnable task) {
        try {
            task.run();
        } catch (Exception e) {
            log.error("[智能预警] {} 执行失败: {}", taskName, e.getMessage(), e);
        }
    }
}

package com.yuwan.completebackend.scheduler;

import com.yuwan.completebackend.service.INotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 站内消息清理定时任务
 * 默认每日凌晨执行，清理超过30天或已过期消息
 *
 * @author 系统架构师
 * @version 1.0
 * @since 2026-03-21
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationCleanupScheduler {

    private final INotificationService notificationService;

    /**
     * 每天凌晨 02:30 执行消息清理
     */
    @Scheduled(cron = "0 30 2 * * ?")
    public void cleanupExpiredMessages() {
        int cleaned = notificationService.cleanupExpiredMessages();
        if (cleaned > 0) {
            log.info("站内消息清理完成，清理条数={}", cleaned);
        }
    }
}

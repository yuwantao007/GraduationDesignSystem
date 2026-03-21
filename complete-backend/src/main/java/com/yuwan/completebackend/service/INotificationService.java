package com.yuwan.completebackend.service;

import com.yuwan.completebackend.common.PageResult;
import com.yuwan.completebackend.model.dto.BatchReadNotificationDTO;
import com.yuwan.completebackend.model.vo.NotificationQueryVO;
import com.yuwan.completebackend.model.vo.NotificationVO;

/**
 * 站内消息服务接口
 *
 * @author 系统架构师
 * @version 1.0
 * @since 2026-03-21
 */
public interface INotificationService {

    /**
     * 分页查询我的消息
     */
    PageResult<NotificationVO> getMyNotifications(NotificationQueryVO queryVO);

    /**
     * 获取未读数量
     */
    long getUnreadCount();

    /**
     * 获取消息详情
     */
    NotificationVO getDetail(String messageId);

    /**
     * 单条标记已读
     */
    void markAsRead(String messageId);

    /**
     * 批量标记已读
     */
    void batchMarkAsRead(BatchReadNotificationDTO dto);

    /**
     * 全部标记已读
     */
    void markAllAsRead();

    /**
     * 标记已处理
     */
    void markAsProcessed(String messageId);

    /**
     * 删除消息
     */
    void deleteMessage(String messageId);

    /**
     * 清理过期和超期消息
     *
     * @return 清理条数
     */
    int cleanupExpiredMessages();
}

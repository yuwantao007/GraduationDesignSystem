package com.yuwan.completebackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yuwan.completebackend.common.PageResult;
import com.yuwan.completebackend.exception.BusinessException;
import com.yuwan.completebackend.mapper.NotificationActionLogMapper;
import com.yuwan.completebackend.mapper.NotificationMessageMapper;
import com.yuwan.completebackend.model.dto.BatchReadNotificationDTO;
import com.yuwan.completebackend.model.entity.NotificationActionLog;
import com.yuwan.completebackend.model.entity.NotificationMessage;
import com.yuwan.completebackend.model.enums.NotificationCategoryEnum;
import com.yuwan.completebackend.model.enums.NotificationLevelEnum;
import com.yuwan.completebackend.model.enums.NotificationStatusEnum;
import com.yuwan.completebackend.model.vo.NotificationQueryVO;
import com.yuwan.completebackend.model.vo.NotificationVO;
import com.yuwan.completebackend.security.SecurityUtil;
import com.yuwan.completebackend.service.INotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 站内消息服务实现
 *
 * @author 系统架构师
 * @version 1.0
 * @since 2026-03-21
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
public class NotificationServiceImpl implements INotificationService {

    private final NotificationMessageMapper notificationMessageMapper;
    private final NotificationActionLogMapper notificationActionLogMapper;

    @Override
    public PageResult<NotificationVO> getMyNotifications(NotificationQueryVO queryVO) {
        String currentUserId = SecurityUtil.getCurrentUserId();
        if (!StringUtils.hasText(currentUserId)) {
            throw new BusinessException("用户未登录");
        }

        Page<NotificationMessage> page = new Page<>(queryVO.getPageNum(), queryVO.getPageSize());
        LambdaQueryWrapper<NotificationMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(NotificationMessage::getReceiverId, currentUserId)
                .eq(NotificationMessage::getDeleted, 0)
                .orderByDesc(NotificationMessage::getCreateTime);

        if (StringUtils.hasText(queryVO.getCategory())) {
            wrapper.eq(NotificationMessage::getCategory, queryVO.getCategory());
        }
        if (queryVO.getLevel() != null) {
            wrapper.eq(NotificationMessage::getLevel, queryVO.getLevel());
        }
        if (queryVO.getStatus() != null) {
            wrapper.eq(NotificationMessage::getMessageStatus, queryVO.getStatus());
        }
        if (StringUtils.hasText(queryVO.getKeyword())) {
            wrapper.and(w -> w.like(NotificationMessage::getTitle, queryVO.getKeyword())
                    .or().like(NotificationMessage::getContent, queryVO.getKeyword()));
        }

        Page<NotificationMessage> result = notificationMessageMapper.selectPage(page, wrapper);
        List<NotificationVO> records = result.getRecords().stream().map(this::convertToVO).collect(Collectors.toList());
        return new PageResult<>(records, result.getTotal(), result.getCurrent(), result.getSize());
    }

    @Override
    public long getUnreadCount() {
        String currentUserId = SecurityUtil.getCurrentUserId();
        if (!StringUtils.hasText(currentUserId)) {
            return 0L;
        }
        return notificationMessageMapper.selectCount(
                new LambdaQueryWrapper<NotificationMessage>()
                        .eq(NotificationMessage::getReceiverId, currentUserId)
                        .eq(NotificationMessage::getMessageStatus, NotificationStatusEnum.UNREAD.getCode())
                        .eq(NotificationMessage::getDeleted, 0)
        );
    }

    @Override
    public NotificationVO getDetail(String messageId) {
        NotificationMessage message = getOwnedMessage(messageId);
        return convertToVO(message);
    }

    @Override
    public void markAsRead(String messageId) {
        NotificationMessage message = getOwnedMessage(messageId);
        if (NotificationStatusEnum.UNREAD.getCode().equals(message.getMessageStatus())) {
            notificationMessageMapper.update(null,
                    new LambdaUpdateWrapper<NotificationMessage>()
                            .set(NotificationMessage::getMessageStatus, NotificationStatusEnum.READ.getCode())
                            .set(NotificationMessage::getReadTime, new Date())
                            .eq(NotificationMessage::getMessageId, messageId)
                            .eq(NotificationMessage::getReceiverId, SecurityUtil.getCurrentUserId())
                            .eq(NotificationMessage::getDeleted, 0)
            );
            saveActionLog(messageId, "READ", "USER");
        }
    }

    @Override
    public void batchMarkAsRead(BatchReadNotificationDTO dto) {
        String currentUserId = SecurityUtil.getCurrentUserId();
        if (!StringUtils.hasText(currentUserId)) {
            throw new BusinessException("用户未登录");
        }

        List<String> messageIds = dto.getMessageIds();
        if (messageIds == null || messageIds.isEmpty()) {
            return;
        }

        notificationMessageMapper.update(null,
                new LambdaUpdateWrapper<NotificationMessage>()
                        .set(NotificationMessage::getMessageStatus, NotificationStatusEnum.READ.getCode())
                        .set(NotificationMessage::getReadTime, new Date())
                        .in(NotificationMessage::getMessageId, messageIds)
                        .eq(NotificationMessage::getReceiverId, currentUserId)
                        .eq(NotificationMessage::getMessageStatus, NotificationStatusEnum.UNREAD.getCode())
                        .eq(NotificationMessage::getDeleted, 0)
        );

        for (String messageId : messageIds) {
            saveActionLog(messageId, "READ", "USER");
        }
    }

    @Override
    public void markAllAsRead() {
        String currentUserId = SecurityUtil.getCurrentUserId();
        if (!StringUtils.hasText(currentUserId)) {
            throw new BusinessException("用户未登录");
        }

        List<NotificationMessage> unreadList = notificationMessageMapper.selectList(
                new LambdaQueryWrapper<NotificationMessage>()
                        .select(NotificationMessage::getMessageId)
                        .eq(NotificationMessage::getReceiverId, currentUserId)
                        .eq(NotificationMessage::getMessageStatus, NotificationStatusEnum.UNREAD.getCode())
                        .eq(NotificationMessage::getDeleted, 0)
        );

        notificationMessageMapper.update(null,
                new LambdaUpdateWrapper<NotificationMessage>()
                        .set(NotificationMessage::getMessageStatus, NotificationStatusEnum.READ.getCode())
                        .set(NotificationMessage::getReadTime, new Date())
                        .eq(NotificationMessage::getReceiverId, currentUserId)
                        .eq(NotificationMessage::getMessageStatus, NotificationStatusEnum.UNREAD.getCode())
                        .eq(NotificationMessage::getDeleted, 0)
        );

        for (NotificationMessage message : unreadList) {
            saveActionLog(message.getMessageId(), "READ_ALL", "USER");
        }
    }

    @Override
    public void markAsProcessed(String messageId) {
        NotificationMessage message = getOwnedMessage(messageId);

        notificationMessageMapper.update(null,
                new LambdaUpdateWrapper<NotificationMessage>()
                        .set(NotificationMessage::getMessageStatus, NotificationStatusEnum.PROCESSED.getCode())
                        .set(NotificationMessage::getProcessedTime, new Date())
                        .setSql("read_time = IFNULL(read_time, NOW())")
                        .eq(NotificationMessage::getMessageId, messageId)
                        .eq(NotificationMessage::getReceiverId, SecurityUtil.getCurrentUserId())
                        .eq(NotificationMessage::getDeleted, 0)
        );

        saveActionLog(message.getMessageId(), "PROCESS", "USER");
    }

    @Override
    public void deleteMessage(String messageId) {
        NotificationMessage message = getOwnedMessage(messageId);

        notificationMessageMapper.update(null,
                new LambdaUpdateWrapper<NotificationMessage>()
                        .set(NotificationMessage::getDeleted, 1)
                        .eq(NotificationMessage::getMessageId, messageId)
                        .eq(NotificationMessage::getReceiverId, SecurityUtil.getCurrentUserId())
                        .eq(NotificationMessage::getDeleted, 0)
        );

        saveActionLog(message.getMessageId(), "DELETE", "USER");
    }

    @Override
    public int cleanupExpiredMessages() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, -30);
        Date threshold = calendar.getTime();

        List<NotificationMessage> toDelete = notificationMessageMapper.selectList(
                new LambdaQueryWrapper<NotificationMessage>()
                        .select(NotificationMessage::getMessageId)
                        .eq(NotificationMessage::getDeleted, 0)
                        .and(w -> w.lt(NotificationMessage::getCreateTime, threshold)
                                .or().lt(NotificationMessage::getExpireTime, new Date()))
        );

        if (toDelete.isEmpty()) {
            return 0;
        }

        List<String> messageIds = toDelete.stream().map(NotificationMessage::getMessageId).collect(Collectors.toList());
        notificationMessageMapper.update(null,
                new LambdaUpdateWrapper<NotificationMessage>()
                        .set(NotificationMessage::getDeleted, 1)
                        .in(NotificationMessage::getMessageId, messageIds)
        );

        for (String messageId : messageIds) {
            saveActionLog(messageId, "DELETE", "SYSTEM");
        }

        return messageIds.size();
    }

    private NotificationMessage getOwnedMessage(String messageId) {
        String currentUserId = SecurityUtil.getCurrentUserId();
        if (!StringUtils.hasText(currentUserId)) {
            throw new BusinessException("用户未登录");
        }

        NotificationMessage message = notificationMessageMapper.selectById(messageId);
        if (message == null || message.getDeleted() == 1) {
            throw new BusinessException("消息不存在");
        }
        if (!currentUserId.equals(message.getReceiverId())) {
            throw new BusinessException("无权访问该消息");
        }
        return message;
    }

    private void saveActionLog(String messageId, String actionType, String actionSource) {
        String currentUserId = SecurityUtil.getCurrentUserId();
        if (!StringUtils.hasText(currentUserId) && "USER".equals(actionSource)) {
            return;
        }

        NotificationActionLog logEntity = new NotificationActionLog();
        logEntity.setMessageId(messageId);
        logEntity.setOperatorId(StringUtils.hasText(currentUserId) ? currentUserId : "SYSTEM");
        logEntity.setActionType(actionType);
        logEntity.setActionSource(actionSource);
        notificationActionLogMapper.insert(logEntity);
    }

    private NotificationVO convertToVO(NotificationMessage message) {
        NotificationVO vo = new NotificationVO();
        BeanUtils.copyProperties(message, vo);
        NotificationCategoryEnum categoryEnum = NotificationCategoryEnum.fromCode(message.getCategory());
        NotificationLevelEnum levelEnum = NotificationLevelEnum.fromCode(message.getLevel());
        vo.setCategoryDesc(categoryEnum != null ? categoryEnum.getDesc() : message.getCategory());
        vo.setLevelDesc(levelEnum != null ? levelEnum.getDesc() : "-");
        return vo;
    }
}

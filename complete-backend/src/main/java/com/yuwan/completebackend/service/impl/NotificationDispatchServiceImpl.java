package com.yuwan.completebackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yuwan.completebackend.mapper.NotificationMessageMapper;
import com.yuwan.completebackend.mapper.NotificationTemplateMapper;
import com.yuwan.completebackend.model.entity.NotificationMessage;
import com.yuwan.completebackend.model.entity.NotificationTemplate;
import com.yuwan.completebackend.model.enums.NotificationStatusEnum;
import com.yuwan.completebackend.service.INotificationDispatchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 站内消息分发服务实现
 *
 * @author 系统架构师
 * @version 1.0
 * @since 2026-03-21
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationDispatchServiceImpl implements INotificationDispatchService {

    private static final Pattern TEMPLATE_PATTERN = Pattern.compile("\\{(\\w+)}");

    private final NotificationTemplateMapper notificationTemplateMapper;
    private final NotificationMessageMapper notificationMessageMapper;

    @Override
    public void sendByTemplateAfterCommit(String templateCode,
                                          String receiverId,
                                          String businessType,
                                          String businessId,
                                          String businessRoute,
                                          Map<String, String> variables,
                                          String dedupKey,
                                          Integer levelOverride,
                                          Date expireTime) {
        if (!StringUtils.hasText(receiverId) || !StringUtils.hasText(templateCode)) {
            return;
        }

        executeAfterCommit(() -> sendOne(templateCode, receiverId, businessType, businessId,
                businessRoute, variables, dedupKey, levelOverride, expireTime));
    }

    @Override
    public void sendBatchByTemplateAfterCommit(String templateCode,
                                               List<String> receiverIds,
                                               String businessType,
                                               String businessId,
                                               String businessRoute,
                                               Map<String, String> variables,
                                               String dedupKeyPrefix,
                                               Integer levelOverride,
                                               Date expireTime) {
        if (!StringUtils.hasText(templateCode) || CollectionUtils.isEmpty(receiverIds)) {
            return;
        }

        executeAfterCommit(() -> {
            for (String receiverId : receiverIds) {
                if (!StringUtils.hasText(receiverId)) {
                    continue;
                }
                String dedupKey = StringUtils.hasText(dedupKeyPrefix)
                        ? dedupKeyPrefix + ":" + receiverId
                        : null;
                sendOne(templateCode, receiverId, businessType, businessId,
                        businessRoute, variables, dedupKey, levelOverride, expireTime);
            }
        });
    }

    private void executeAfterCommit(Runnable runnable) {
        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    runnable.run();
                }
            });
        } else {
            runnable.run();
        }
    }

    private void sendOne(String templateCode,
                         String receiverId,
                         String businessType,
                         String businessId,
                         String businessRoute,
                         Map<String, String> variables,
                         String dedupKey,
                         Integer levelOverride,
                         Date expireTime) {
        try {
            NotificationTemplate template = notificationTemplateMapper.selectOne(
                    new LambdaQueryWrapper<NotificationTemplate>()
                            .eq(NotificationTemplate::getTemplateCode, templateCode)
                            .eq(NotificationTemplate::getIsEnabled, 1)
                            .eq(NotificationTemplate::getDeleted, 0)
                            .last("LIMIT 1")
            );
            if (template == null) {
                log.warn("消息模板不存在或未启用，templateCode={}", templateCode);
                return;
            }

            if (StringUtils.hasText(dedupKey)) {
                Long exist = notificationMessageMapper.selectCount(
                        new LambdaQueryWrapper<NotificationMessage>()
                                .eq(NotificationMessage::getReceiverId, receiverId)
                                .eq(NotificationMessage::getDedupKey, dedupKey)
                                .eq(NotificationMessage::getDeleted, 0)
                );
                if (exist != null && exist > 0) {
                    return;
                }
            }

            NotificationMessage message = new NotificationMessage();
            message.setReceiverId(receiverId);
            message.setCategory(template.getCategory());
            message.setLevel(levelOverride != null ? levelOverride : template.getDefaultLevel());
            message.setTitle(renderTemplate(template.getTitleTemplate(), variables));
            message.setContent(renderTemplate(template.getContentTemplate(), variables));
            message.setBusinessType(businessType);
            message.setBusinessId(businessId);
            message.setBusinessRoute(businessRoute);
            message.setDedupKey(dedupKey);
            message.setMessageStatus(NotificationStatusEnum.UNREAD.getCode());
            message.setExpireTime(expireTime != null ? expireTime : defaultExpireTime());

            notificationMessageMapper.insert(message);
        } catch (Exception e) {
            log.error("发送站内消息失败，templateCode={}, receiverId={}", templateCode, receiverId, e);
        }
    }

    private Date defaultExpireTime() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, 30);
        return calendar.getTime();
    }

    private String renderTemplate(String templateText, Map<String, String> variables) {
        if (!StringUtils.hasText(templateText) || CollectionUtils.isEmpty(variables)) {
            return templateText;
        }
        Matcher matcher = TEMPLATE_PATTERN.matcher(templateText);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            String key = matcher.group(1);
            String replacement = variables.getOrDefault(key, "-");
            matcher.appendReplacement(sb, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(sb);
        return sb.toString();
    }
}

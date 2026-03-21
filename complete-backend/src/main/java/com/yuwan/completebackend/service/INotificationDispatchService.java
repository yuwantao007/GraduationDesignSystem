package com.yuwan.completebackend.service;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 站内消息分发服务接口
 * 提供模板渲染、事务提交后发送、去重等能力
 *
 * @author 系统架构师
 * @version 1.0
 * @since 2026-03-21
 */
public interface INotificationDispatchService {

    /**
     * 事务提交后发送单条消息
     */
    void sendByTemplateAfterCommit(String templateCode,
                                   String receiverId,
                                   String businessType,
                                   String businessId,
                                   String businessRoute,
                                   Map<String, String> variables,
                                   String dedupKey,
                                   Integer levelOverride,
                                   Date expireTime);

    /**
     * 事务提交后批量发送消息
     */
    void sendBatchByTemplateAfterCommit(String templateCode,
                                        List<String> receiverIds,
                                        String businessType,
                                        String businessId,
                                        String businessRoute,
                                        Map<String, String> variables,
                                        String dedupKeyPrefix,
                                        Integer levelOverride,
                                        Date expireTime);
}

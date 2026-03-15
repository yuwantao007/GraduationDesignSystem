package com.yuwan.completebackend.workflow.listener;

import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.delegate.TaskListener;
import org.flowable.identitylink.api.IdentityLink;
import org.flowable.task.service.delegate.DelegateTask;

import java.util.stream.Collectors;

/**
 * 审查任务创建监听器
 * <p>
 * 当新的审查任务被创建时触发，用于记录日志或发送通知。
 * 实际业务通知功能待 RabbitMQ 模块完成后扩展。
 * </p>
 *
 * @author 系统架构师
 * @version 1.0
 * @since 2026-03-15
 */
@Slf4j
public class ReviewTaskCreateListener implements TaskListener {

    @Override
    public void notify(DelegateTask delegateTask) {
        String topicId = (String) delegateTask.getVariable("topicId");
        String topicTitle = (String) delegateTask.getVariable("topicTitle");
        // getCandidates() 返回 Set<IdentityLink>，提取候选组ID列表用于日志
        String candidateGroups = delegateTask.getCandidates().stream()
                .filter(link -> link.getGroupId() != null)
                .map(IdentityLink::getGroupId)
                .collect(Collectors.joining(", "));
        log.info("[Flowable] 新审查任务已创建 - 任务名称: {}, 课题ID: {}, 课题标题: {}, 候选组: {}",
                delegateTask.getName(),
                topicId,
                topicTitle,
                candidateGroups);
    }
}

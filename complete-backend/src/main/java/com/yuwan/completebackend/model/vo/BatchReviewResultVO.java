package com.yuwan.completebackend.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 批量审批结果VO
 *
 * @author 系统架构师
 * @version 1.0
 * @since 2026-02-23
 */
@Data
@Schema(description = "批量审批结果响应")
public class BatchReviewResultVO {

    @Schema(description = "批量审批批次ID")
    private String batchId;

    @Schema(description = "成功数量")
    private Integer successCount;

    @Schema(description = "失败数量")
    private Integer failedCount;

    @Schema(description = "审批总数")
    private Integer totalCount;

    @Schema(description = "成功的课题ID列表")
    private List<String> successTopicIds;

    @Schema(description = "失败的课题信息列表")
    private List<FailedTopicInfo> failedTopics;

    /**
     * 失败课题信息
     */
    @Data
    @Schema(description = "失败课题信息")
    public static class FailedTopicInfo {
        @Schema(description = "课题ID")
        private String topicId;

        @Schema(description = "课题名称")
        private String topicTitle;

        @Schema(description = "失败原因")
        private String failReason;
    }

    /**
     * 初始化
     */
    public BatchReviewResultVO() {
        this.successCount = 0;
        this.failedCount = 0;
        this.totalCount = 0;
        this.successTopicIds = new ArrayList<>();
        this.failedTopics = new ArrayList<>();
    }

    /**
     * 添加成功记录
     *
     * @param topicId 课题ID
     */
    public void addSuccess(String topicId) {
        this.successTopicIds.add(topicId);
        this.successCount++;
        this.totalCount++;
    }

    /**
     * 添加失败记录
     *
     * @param topicId    课题ID
     * @param topicTitle 课题名称
     * @param reason     失败原因
     */
    public void addFailed(String topicId, String topicTitle, String reason) {
        FailedTopicInfo info = new FailedTopicInfo();
        info.setTopicId(topicId);
        info.setTopicTitle(topicTitle);
        info.setFailReason(reason);
        this.failedTopics.add(info);
        this.failedCount++;
        this.totalCount++;
    }
}

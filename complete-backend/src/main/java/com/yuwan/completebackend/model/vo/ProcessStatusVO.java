package com.yuwan.completebackend.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 课题流程状态 VO
 * <p>
 * 课题维度的流程快照，用于课题详情页展示当前流程所处阶段。
 * </p>
 *
 * @author 系统架构师
 * @version 1.0
 * @since 2026-03-15
 */
@Data
@Schema(description = "课题流程状态")
public class ProcessStatusVO {

    @Schema(description = "课题ID")
    private String topicId;

    @Schema(description = "流程实例ID")
    private String processInstanceId;

    @Schema(description = "流程是否已启动")
    private Boolean processStarted;

    @Schema(description = "流程是否已完成")
    private Boolean processFinished;

    @Schema(description = "当前 reviewStatus 值")
    private Integer reviewStatus;

    @Schema(description = "当前 reviewStatus 描述")
    private String reviewStatusDesc;

    @Schema(description = "当前活跃任务节点名称列表")
    private List<String> activeTaskNames;

    @Schema(description = "当前活跃任务等待的角色")
    private String waitingRole;

    @Schema(description = "流程启动时间")
    private LocalDateTime startTime;

    @Schema(description = "流程结束时间（已完成或终止时有值）")
    private LocalDateTime endTime;

    @Schema(description = "历史节点列表（用于时间线展示）")
    private List<HistoryNodeVO> historyNodes;

    /**
     * 历史节点信息
     */
    @Data
    @Schema(description = "历史流程节点")
    public static class HistoryNodeVO {

        @Schema(description = "活动ID（BPMN activityId）")
        private String activityId;

        @Schema(description = "活动名称")
        private String activityName;

        @Schema(description = "活动类型（userTask / exclusiveGateway 等）")
        private String activityType;

        @Schema(description = "执行人userId")
        private String assignee;

        @Schema(description = "执行人姓名")
        private String assigneeName;

        @Schema(description = "开始时间")
        private LocalDateTime startTime;

        @Schema(description = "结束时间（null 表示当前活跃节点）")
        private LocalDateTime endTime;

        @Schema(description = "是否为当前活跃节点")
        private Boolean active;
    }
}

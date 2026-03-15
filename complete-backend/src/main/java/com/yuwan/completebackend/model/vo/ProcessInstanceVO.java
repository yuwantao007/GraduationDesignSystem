package com.yuwan.completebackend.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 流程实例监控 VO
 * <p>
 * 管理员用于监控所有课题审查流程实例的列表项。
 * </p>
 *
 * @author 系统架构师
 * @version 1.0
 * @since 2026-03-15
 */
@Data
@Schema(description = "流程实例监控信息")
public class ProcessInstanceVO {

    @Schema(description = "流程实例ID")
    private String processInstanceId;

    @Schema(description = "课题ID")
    private String topicId;

    @Schema(description = "课题标题")
    private String topicTitle;

    @Schema(description = "课题大类（1-高职升本 2-3+1 3-实验班）")
    private Integer topicCategory;

    @Schema(description = "课题大类描述")
    private String topicCategoryDesc;

    @Schema(description = "课题创建人ID")
    private String creatorId;

    @Schema(description = "课题创建人姓名")
    private String creatorName;

    @Schema(description = "当前 reviewStatus 值")
    private Integer reviewStatus;

    @Schema(description = "当前 reviewStatus 描述")
    private String reviewStatusDesc;

    @Schema(description = "当前活跃任务名称（流程运行中时有值）")
    private String currentTaskName;

    @Schema(description = "当前等待操作的角色")
    private String waitingRole;

    @Schema(description = "流程状态（0-运行中 1-已完成 2-已终止）")
    private Integer processStatus;

    @Schema(description = "流程状态描述")
    private String processStatusDesc;

    @Schema(description = "流程启动时间")
    private LocalDateTime startTime;

    @Schema(description = "流程结束时间")
    private LocalDateTime endTime;

    @Schema(description = "已运行时长描述（如3天2小时）")
    private String durationDesc;
}

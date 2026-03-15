package com.yuwan.completebackend.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 流程待办任务 VO
 * <p>
 * 用于向审核角色展示待办任务列表。
 * </p>
 *
 * @author 系统架构师
 * @version 1.0
 * @since 2026-03-15
 */
@Data
@Schema(description = "流程待办任务")
public class FlowTaskVO {

    @Schema(description = "Flowable 任务ID")
    private String taskId;

    @Schema(description = "任务定义Key（BPMN 中的 id）")
    private String taskDefKey;

    @Schema(description = "任务名称（BPMN 中的 name）")
    private String taskName;

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

    @Schema(description = "课题创建人ID（企业教师）")
    private String creatorId;

    @Schema(description = "课题创建人姓名")
    private String creatorName;

    @Schema(description = "任务签收人（已签收则为签收人 userId）")
    private String assignee;

    @Schema(description = "任务签收人姓名")
    private String assigneeName;

    @Schema(description = "候选组（角色代码）")
    private String candidateGroup;

    @Schema(description = "任务创建时间")
    private LocalDateTime createTime;

    @Schema(description = "是否已被当前用户签收")
    private Boolean claimedByMe;

    @Schema(description = "是否是修改任务（企业教师的修改待重提任务）")
    private Boolean isModifyTask;
}

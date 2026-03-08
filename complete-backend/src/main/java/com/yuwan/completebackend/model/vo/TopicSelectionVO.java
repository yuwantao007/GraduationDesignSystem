package com.yuwan.completebackend.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 课题选报记录响应
 * 用于"我的选报"列表展示
 *
 * @author 系统架构师
 * @version 1.0
 * @since 2026-03-08
 */
@Data
@Schema(description = "课题选报记录响应")
public class TopicSelectionVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "选报ID")
    private String selectionId;

    @Schema(description = "课题ID")
    private String topicId;

    @Schema(description = "课题名称")
    private String topicTitle;

    @Schema(description = "课题大类")
    private Integer topicCategory;

    @Schema(description = "课题大类描述")
    private String topicCategoryDesc;

    @Schema(description = "归属企业名称")
    private String enterpriseName;

    @Schema(description = "指导方向/专业")
    private String guidanceDirection;

    @Schema(description = "企业教师姓名（课题创建人）")
    private String creatorName;

    @Schema(description = "选报理由")
    private String selectionReason;

    @Schema(description = "选报状态（0-待确认 1-中选 2-落选）")
    private Integer selectionStatus;

    @Schema(description = "选报状态描述")
    private String selectionStatusDesc;

    @Schema(description = "选报时间")
    private String applyTime;

    @Schema(description = "确认时间")
    private String confirmTime;
}

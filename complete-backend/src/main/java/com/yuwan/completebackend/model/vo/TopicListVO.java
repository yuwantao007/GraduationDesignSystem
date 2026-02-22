package com.yuwan.completebackend.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 课题列表项VO
 * 用于返回课题列表简要信息
 *
 * @author 系统架构师
 * @version 1.0
 * @since 2026-02-21
 */
@Data
@Schema(description = "课题列表项响应")
public class TopicListVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "课题ID")
    private String topicId;

    @Schema(description = "课题名称/题目")
    private String topicTitle;

    @Schema(description = "课题大类")
    private Integer topicCategory;

    @Schema(description = "课题大类描述")
    private String topicCategoryDesc;

    @Schema(description = "课题类型")
    private Integer topicType;

    @Schema(description = "课题类型描述")
    private String topicTypeDesc;

    @Schema(description = "课题来源")
    private Integer topicSource;

    @Schema(description = "课题来源描述")
    private String topicSourceDesc;

    @Schema(description = "归属企业名称")
    private String enterpriseName;

    @Schema(description = "指导方向/专业")
    private String guidanceDirection;

    @Schema(description = "创建人姓名")
    private String creatorName;

    @Schema(description = "审查状态")
    private Integer reviewStatus;

    @Schema(description = "审查状态描述")
    private String reviewStatusDesc;

    @Schema(description = "是否已提交")
    private Integer isSubmitted;

    @Schema(description = "创建时间")
    private String createTime;
}

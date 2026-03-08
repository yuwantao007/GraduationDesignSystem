package com.yuwan.completebackend.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 可选课题列表项响应
 * 面向学生的课题选报浏览列表
 *
 * @author 系统架构师
 * @version 1.0
 * @since 2026-03-08
 */
@Data
@Schema(description = "可选课题列表项响应")
public class TopicForSelectionVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "课题ID")
    private String topicId;

    @Schema(description = "课题名称")
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

    @Schema(description = "课题所属专业ID")
    private String majorId;

    @Schema(description = "课题所属专业名称")
    private String majorName;

    @Schema(description = "指导方向/专业")
    private String guidanceDirection;

    @Schema(description = "企业教师姓名（创建人）")
    private String creatorName;

    @Schema(description = "课题内容简述")
    private String contentSummary;

    @Schema(description = "已选报人数（待确认+中选）")
    private Integer selectedCount;

    @Schema(description = "当前学生是否已选报此课题")
    private Boolean alreadyApplied;
}

package com.yuwan.completebackend.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 课题查询参数VO
 * 用于接收课题列表查询条件
 *
 * @author 系统架构师
 * @version 1.0
 * @since 2026-02-21
 */
@Data
@Schema(description = "课题查询参数")
public class TopicQueryVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "课题名称（模糊查询）")
    private String topicTitle;

    @Schema(description = "课题大类（1-高职升本 2-3+1 3-实验班）")
    private Integer topicCategory;

    @Schema(description = "课题类型（1-设计 2-论文）")
    private Integer topicType;

    @Schema(description = "课题来源（1-校内 2-校外协同开发）")
    private Integer topicSource;

    @Schema(description = "归属企业ID")
    private String enterpriseId;

    @Schema(description = "课题所属专业ID（学生角色将由后端自动注入）")
    private String majorId;

    @Schema(description = "指导方向/专业（模糊查询）")
    private String guidanceDirection;

    @Schema(description = "创建人ID（企业教师ID）")
    private String creatorId;

    @Schema(description = "审查状态")
    private Integer reviewStatus;

    @Schema(description = "是否已提交（0-未提交 1-已提交）")
    private Integer isSubmitted;

    @Schema(description = "页码", example = "1")
    private Integer pageNum = 1;

    @Schema(description = "每页大小", example = "10")
    private Integer pageSize = 10;
}

package com.yuwan.completebackend.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 待审查课题查询参数VO
 *
 * @author 系统架构师
 * @version 1.0
 * @since 2026-02-23
 */
@Data
@Schema(description = "待审查课题查询参数")
public class ReviewQueryVO {

    @Schema(description = "课题名称（模糊查询）")
    private String topicTitle;

    @Schema(description = "课题大类（1-高职升本 2-3+1 3-实验班）")
    private Integer topicCategory;

    @Schema(description = "指导方向/专业")
    private String guidanceDirection;

    @Schema(description = "创建人（企业教师）ID")
    private String creatorId;

    @Schema(description = "创建人姓名（模糊查询）")
    private String creatorName;

    @Schema(description = "归属企业ID")
    private String enterpriseId;

    @Schema(description = "审查状态（用于查看特定状态的课题）")
    private Integer reviewStatus;

    @Schema(description = "页码", example = "1")
    private Integer pageNum = 1;

    @Schema(description = "每页大小", example = "10")
    private Integer pageSize = 10;
}

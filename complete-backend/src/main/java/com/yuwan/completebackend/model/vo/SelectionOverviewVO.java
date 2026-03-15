package com.yuwan.completebackend.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 双选概览VO（企业负责人视角）
 * 用于企业负责人查看每门课题的选报与确认情况
 *
 * @author 系统架构师
 * @version 1.0
 * @since 2026-03-14
 */
@Data
@Schema(description = "双选概览响应（课题维度）")
public class SelectionOverviewVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "课题ID")
    private String topicId;

    @Schema(description = "课题名称")
    private String topicTitle;

    @Schema(description = "课题大类（1-高职升本 2-3+1 3-实验班）")
    private Integer topicCategory;

    @Schema(description = "课题大类描述")
    private String topicCategoryDesc;

    @Schema(description = "课题来源（1-校内 2-校外协同开发）")
    private Integer topicSource;

    @Schema(description = "课题来源描述")
    private String topicSourceDesc;

    @Schema(description = "企业教师ID（课题创建人）")
    private String creatorId;

    @Schema(description = "企业教师姓名")
    private String creatorName;

    @Schema(description = "指导方向")
    private String guidanceDirection;

    @Schema(description = "选报总人数（待确认+中选）")
    private Integer totalApplicants;

    @Schema(description = "已确认人数（中选）")
    private Integer confirmedCount;

    @Schema(description = "待确认人数")
    private Integer pendingCount;

    @Schema(description = "落选人数")
    private Integer rejectedCount;
}

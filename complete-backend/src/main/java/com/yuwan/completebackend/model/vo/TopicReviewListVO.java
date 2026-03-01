package com.yuwan.completebackend.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * 待审查课题列表VO
 *
 * @author 系统架构师
 * @version 1.0
 * @since 2026-02-23
 */
@Data
@Schema(description = "待审查课题列表响应")
public class TopicReviewListVO {

    @Schema(description = "课题ID")
    private String topicId;

    @Schema(description = "课题名称")
    private String topicTitle;

    @Schema(description = "课题大类（1-高职升本 2-3+1 3-实验班）")
    private Integer topicCategory;

    @Schema(description = "课题大类名称")
    private String topicCategoryName;

    @Schema(description = "课题类型（1-设计 2-论文）")
    private Integer topicType;

    @Schema(description = "课题类型名称")
    private String topicTypeName;

    @Schema(description = "指导方向/专业")
    private String guidanceDirection;

    @Schema(description = "归属企业ID")
    private String enterpriseId;

    @Schema(description = "归属企业名称")
    private String enterpriseName;

    @Schema(description = "创建人ID（企业教师）")
    private String creatorId;

    @Schema(description = "创建人姓名")
    private String creatorName;

    @Schema(description = "当前审查状态")
    private Integer reviewStatus;

    @Schema(description = "当前审查状态名称")
    private String reviewStatusName;

    @Schema(description = "是否可审批")
    private Boolean canReview;

    @Schema(description = "提交时间")
    private Date submitTime;

    @Schema(description = "最近审查时间")
    private Date lastReviewTime;

    @Schema(description = "已有审查记录数")
    private Integer reviewCount;

    @Schema(description = "最近审查意见（简略）")
    private String lastReviewOpinion;

    @Schema(description = "历史审查记录列表")
    private List<TopicReviewRecordVO> reviewHistory;
}

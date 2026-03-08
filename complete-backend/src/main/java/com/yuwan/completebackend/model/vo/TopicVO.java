package com.yuwan.completebackend.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * 课题信息响应VO
 * 用于返回课题详细信息
 *
 * @author 系统架构师
 * @version 1.0
 * @since 2026-02-21
 */
@Data
@Schema(description = "课题信息响应")
public class TopicVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "课题ID")
    private String topicId;

    @Schema(description = "课题名称/题目")
    private String topicTitle;

    @Schema(description = "课题大类（1-高职升本 2-3+1 3-实验班）")
    private Integer topicCategory;

    @Schema(description = "课题大类描述")
    private String topicCategoryDesc;

    @Schema(description = "课题类型（1-设计 2-论文）")
    private Integer topicType;

    @Schema(description = "课题类型描述")
    private String topicTypeDesc;

    @Schema(description = "课题来源（1-校内 2-校外协同开发）")
    private Integer topicSource;

    @Schema(description = "课题来源描述")
    private String topicSourceDesc;

    @Schema(description = "适用学校")
    private String applicableSchool;

    @Schema(description = "归属企业ID（高职升本课题）")
    private String enterpriseId;

    @Schema(description = "归属企业名称（高职升本课题）")
    private String enterpriseName;

    @Schema(description = "课题所属专业ID")
    private String majorId;

    @Schema(description = "课题所属专业名称")
    private String majorName;

    @Schema(description = "关联学校ID（3+1/实验班课题）")
    private String schoolId;

    @Schema(description = "关联学校名称（3+1/实验班课题）")
    private String schoolName;

    @Schema(description = "指导方向/专业")
    private String guidanceDirection;

    @Schema(description = "选题背景与意义")
    private String backgroundSignificance;

    @Schema(description = "课题内容简述")
    private String contentSummary;

    @Schema(description = "专业知识综合训练情况")
    private String professionalTraining;

    @Schema(description = "开发环境(工具)")
    private Map<String, String> developmentEnvironment;

    @Schema(description = "工作量总周数")
    private Integer workloadWeeks;

    @Schema(description = "工作量明细")
    private List<Map<String, Object>> workloadDetail;

    @Schema(description = "任务与进度要求")
    private List<Map<String, Object>> scheduleRequirements;

    @Schema(description = "主要参考文献")
    private List<Map<String, Object>> topicReferences;

    @Schema(description = "起止日期-开始")
    private String startDate;

    @Schema(description = "起止日期-结束")
    private String endDate;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "创建人ID")
    private String creatorId;

    @Schema(description = "创建人姓名")
    private String creatorName;

    @Schema(description = "学院负责人签名")
    private String collegeLeaderSign;

    @Schema(description = "学院负责人签名时间")
    private String collegeLeaderSignTime;

    @Schema(description = "企业负责人签名")
    private String enterpriseLeaderSign;

    @Schema(description = "企业负责人签名时间")
    private String enterpriseLeaderSignTime;

    @Schema(description = "企业指导教师签名")
    private String enterpriseTeacherSign;

    @Schema(description = "企业指导教师签名时间")
    private String enterpriseTeacherSignTime;

    @Schema(description = "审查状态")
    private Integer reviewStatus;

    @Schema(description = "审查状态描述")
    private String reviewStatusDesc;

    @Schema(description = "是否已提交")
    private Integer isSubmitted;

    @Schema(description = "创建时间")
    private String createTime;

    @Schema(description = "更新时间")
    private String updateTime;
}

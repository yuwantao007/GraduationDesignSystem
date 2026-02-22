package com.yuwan.completebackend.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 创建课题请求DTO
 * 企业教师创建课题申报时使用
 *
 * @author 系统架构师
 * @version 1.0
 * @since 2026-02-21
 */
@Data
@Schema(description = "创建课题请求参数")
public class CreateTopicDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotBlank(message = "课题名称不能为空")
    @Size(max = 100, message = "课题名称长度不能超过100个字符（中文算2个字符，建议不超过50字符）")
    @Schema(description = "课题名称/题目", requiredMode = Schema.RequiredMode.REQUIRED)
    private String topicTitle;

    @NotNull(message = "课题大类不能为空")
    @Min(value = 1, message = "课题大类值无效")
    @Max(value = 3, message = "课题大类值无效")
    @Schema(description = "课题大类（1-高职升本 2-3+1 3-实验班）", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer topicCategory;

    @NotNull(message = "课题类型不能为空")
    @Min(value = 1, message = "课题类型值无效")
    @Max(value = 2, message = "课题类型值无效")
    @Schema(description = "课题类型（1-设计 2-论文）", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer topicType;

    @NotNull(message = "课题来源不能为空")
    @Min(value = 1, message = "课题来源值无效")
    @Max(value = 2, message = "课题来源值无效")
    @Schema(description = "课题来源（1-校内 2-校外协同开发）", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer topicSource;

    @Schema(description = "适用学校（3+1/实验班必填）")
    private String applicableSchool;

    @NotBlank(message = "归属企业不能为空")
    @Schema(description = "归属企业ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private String enterpriseId;

    @Schema(description = "指导方向/专业")
    private String guidanceDirection;

    @NotBlank(message = "选题背景与意义不能为空")
    @Size(min = 150, message = "选题背景与意义不少于150字")
    @Schema(description = "选题背景与意义（≥150字）", requiredMode = Schema.RequiredMode.REQUIRED)
    private String backgroundSignificance;

    @NotBlank(message = "课题内容简述不能为空")
    @Size(min = 150, message = "课题内容简述不少于150字")
    @Schema(description = "课题内容简述（≥150字）", requiredMode = Schema.RequiredMode.REQUIRED)
    private String contentSummary;

    @NotBlank(message = "专业知识综合训练情况不能为空")
    @Size(min = 100, message = "专业知识综合训练情况不少于100字")
    @Schema(description = "专业知识综合训练情况（≥100字）", requiredMode = Schema.RequiredMode.REQUIRED)
    private String professionalTraining;

    @Schema(description = "开发环境(工具)")
    private Map<String, String> developmentEnvironment;

    @Schema(description = "工作量总周数", example = "17")
    private Integer workloadWeeks = 17;

    @Schema(description = "工作量明细")
    private List<Map<String, Object>> workloadDetail;

    @Schema(description = "任务与进度要求")
    private List<Map<String, Object>> scheduleRequirements;

    @Schema(description = "主要参考文献")
    private List<Map<String, Object>> topicReferences;

    @Schema(description = "起止日期-开始")
    private Date startDate;

    @Schema(description = "起止日期-结束")
    private Date endDate;

    @Size(max = 500, message = "备注长度不能超过500个字符")
    @Schema(description = "备注")
    private String remark;
}

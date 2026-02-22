package com.yuwan.completebackend.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 课题信息实体类
 * 对应数据库表 topic_info
 *
 * @author 系统架构师
 * @version 1.0
 * @since 2026-02-21
 */
@Data
@TableName(value = "topic_info", autoResultMap = true)
@Schema(description = "课题信息实体")
public class Topic implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "topic_id", type = IdType.ASSIGN_ID)
    @Schema(description = "课题ID")
    private String topicId;

    @Schema(description = "课题名称/题目")
    private String topicTitle;

    @Schema(description = "课题大类（1-高职升本 2-3+1 3-实验班）")
    private Integer topicCategory;

    @Schema(description = "课题类型（1-设计 2-论文）")
    private Integer topicType;

    @Schema(description = "课题来源（1-校内 2-校外协同开发）")
    private Integer topicSource;

    @Schema(description = "适用学校")
    private String applicableSchool;

    @Schema(description = "归属企业ID")
    private String enterpriseId;

    @Schema(description = "指导方向/专业")
    private String guidanceDirection;

    @Schema(description = "选题背景与意义")
    private String backgroundSignificance;

    @Schema(description = "课题内容简述")
    private String contentSummary;

    @Schema(description = "专业知识综合训练情况")
    private String professionalTraining;

    @TableField(typeHandler = JacksonTypeHandler.class)
    @Schema(description = "开发环境(工具)")
    private Map<String, String> developmentEnvironment;

    @Schema(description = "工作量总周数")
    private Integer workloadWeeks;

    @TableField(typeHandler = JacksonTypeHandler.class)
    @Schema(description = "工作量明细")
    private List<Map<String, Object>> workloadDetail;

    @TableField(typeHandler = JacksonTypeHandler.class)
    @Schema(description = "任务与进度要求")
    private List<Map<String, Object>> scheduleRequirements;

    @TableField(typeHandler = JacksonTypeHandler.class)
    @Schema(description = "主要参考文献")
    private List<Map<String, Object>> topicReferences;

    @Schema(description = "起止日期-开始")
    private Date startDate;

    @Schema(description = "起止日期-结束")
    private Date endDate;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "创建人(企业教师ID)")
    private String creatorId;

    @Schema(description = "学院负责人签名（图片URL）")
    private String collegeLeaderSign;

    @Schema(description = "学院负责人签名时间")
    private Date collegeLeaderSignTime;

    @Schema(description = "企业负责人签名（图片URL）")
    private String enterpriseLeaderSign;

    @Schema(description = "企业负责人签名时间")
    private Date enterpriseLeaderSignTime;

    @Schema(description = "企业指导教师签名（图片URL）")
    private String enterpriseTeacherSign;

    @Schema(description = "企业指导教师签名时间")
    private Date enterpriseTeacherSignTime;

    @Schema(description = "审查状态（0-草稿 1-待预审 2-预审通过 3-预审需修改 4-初审通过 5-初审需修改 6-终审通过 7-终审不通过）")
    private Integer reviewStatus;

    @Schema(description = "是否已提交（0-未提交 1-已提交）")
    private Integer isSubmitted;

    @TableLogic
    @Schema(description = "逻辑删除（0-未删除 1-已删除）")
    private Integer deleted;

    @TableField(fill = FieldFill.INSERT)
    @Schema(description = "创建时间")
    private Date createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    @Schema(description = "更新时间")
    private Date updateTime;
}

package com.yuwan.completebackend.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * 中期检查表实体类
 * 对应数据库表 midterm_check
 *
 * @author 系统架构师
 * @version 1.0
 * @since 2026-03-17
 */
@Data
@TableName("midterm_check")
@Schema(description = "中期检查表实体")
public class MidtermCheck implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "check_id", type = IdType.ASSIGN_ID)
    @Schema(description = "检查表ID")
    private String checkId;

    @Schema(description = "学生ID（每个学生一份）")
    private String studentId;

    @Schema(description = "课题ID")
    private String topicId;

    @Schema(description = "对应中期答辩安排ID")
    private String arrangementId;

    @Schema(description = "填写企业教师ID")
    private String enterpriseTeacherId;

    @Schema(description = "完成情况（企业教师填写）")
    private String completionStatus;

    @Schema(description = "存在问题")
    private String existingProblems;

    @Schema(description = "下一步计划")
    private String nextPlan;

    @Schema(description = "中期检查表附件ID（关联document_info）")
    private String documentId;

    @Schema(description = "提交状态: 0=草稿, 1=已提交")
    private Integer submitStatus;

    @Schema(description = "高校教师审查状态: 0=未审, 1=合格, 2=不合格")
    private Integer reviewStatus;

    @Schema(description = "高校教师审查意见")
    private String reviewComment;

    @Schema(description = "审查人（高校教师ID）")
    private String reviewerId;

    @Schema(description = "审查时间")
    private Date reviewTime;

    @TableLogic
    @TableField("is_deleted")
    @Schema(description = "逻辑删除: 0=正常, 1=删除")
    private Integer deleted;

    @TableField(fill = FieldFill.INSERT)
    @Schema(description = "创建时间")
    private Date createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    @Schema(description = "更新时间")
    private Date updateTime;
}


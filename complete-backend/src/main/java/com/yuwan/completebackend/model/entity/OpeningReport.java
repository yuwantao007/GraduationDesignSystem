package com.yuwan.completebackend.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * 开题报告实体类
 * 对应数据库表 opening_report
 *
 * @author 系统架构师
 * @version 1.0
 * @since 2026-03-17
 */
@Data
@TableName("opening_report")
@Schema(description = "开题报告实体")
public class OpeningReport implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "report_id", type = IdType.ASSIGN_ID)
    @Schema(description = "报告ID")
    private String reportId;

    @Schema(description = "学生ID（一个学生只有一份）")
    private String studentId;

    @Schema(description = "课题ID")
    private String topicId;

    @Schema(description = "对应答辩安排ID")
    private String arrangementId;

    @Schema(description = "报告文件ID（关联document_info）")
    private String documentId;

    @Schema(description = "提交时间")
    private Date submitTime;

    @Schema(description = "审查状态: 0=未提交, 1=已提交待审, 2=通过, 3=不合格")
    private Integer reviewStatus;

    @Schema(description = "审查意见")
    private String reviewComment;

    @Schema(description = "审查人ID（企业教师）")
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


package com.yuwan.completebackend.model.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * 中期检查表列表VO
 *
 * @author 系统架构师
 * @version 1.0
 * @since 2026-03-17
 */
@Data
@Schema(description = "中期检查表列表视图对象")
public class MidtermCheckListVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "检查表ID")
    private String checkId;

    @Schema(description = "学生ID")
    private String studentId;

    @Schema(description = "学生姓名")
    private String studentName;

    @Schema(description = "学号")
    private String studentNo;

    @Schema(description = "课题名称")
    private String topicName;

    @Schema(description = "企业教师姓名")
    private String enterpriseTeacherName;

    @Schema(description = "提交状态: 0=草稿, 1=已提交")
    private Integer submitStatus;

    @Schema(description = "提交状态描述")
    private String submitStatusDesc;

    @Schema(description = "审查状态: 0=未审, 1=合格, 2=不合格")
    private Integer reviewStatus;

    @Schema(description = "审查状态描述")
    private String reviewStatusDesc;

    @Schema(description = "审查人姓名")
    private String reviewerName;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Schema(description = "审查时间")
    private Date reviewTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Schema(description = "创建时间")
    private Date createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Schema(description = "更新时间")
    private Date updateTime;
}

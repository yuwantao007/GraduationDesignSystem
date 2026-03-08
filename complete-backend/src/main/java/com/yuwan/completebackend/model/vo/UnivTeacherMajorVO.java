package com.yuwan.completebackend.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 高校教师-专业方向分配响应
 *
 * @author 系统架构师
 * @version 1.0
 * @since 2026-03-08
 */
@Data
@Schema(description = "方向级分配信息响应")
public class UnivTeacherMajorVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "主键ID")
    private String id;

    @Schema(description = "高校教师 user_id")
    private String univTeacherId;

    @Schema(description = "高校教师姓名")
    private String univTeacherName;

    @Schema(description = "高校教师工号")
    private String univTeacherEmployeeNo;

    @Schema(description = "专业方向ID")
    private String directionId;

    @Schema(description = "专业方向名称")
    private String directionName;

    @Schema(description = "所属企业ID")
    private String enterpriseId;

    @Schema(description = "企业名称")
    private String enterpriseName;

    @Schema(description = "届别")
    private String cohort;

    @Schema(description = "是否启用（1-启用 0-停用）")
    private Integer isEnabled;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "创建时间")
    private String createTime;
}

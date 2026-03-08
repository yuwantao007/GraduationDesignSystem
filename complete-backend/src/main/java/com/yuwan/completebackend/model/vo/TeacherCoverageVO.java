package com.yuwan.completebackend.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 教师配对覆盖率统计响应
 *
 * @author 系统架构师
 * @version 1.0
 * @since 2026-03-08
 */
@Data
@Schema(description = "教师配对覆盖率统计")
public class TeacherCoverageVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "企业教师 user_id")
    private String enterpriseTeacherId;

    @Schema(description = "企业教师姓名")
    private String enterpriseTeacherName;

    @Schema(description = "企业教师工号")
    private String enterpriseTeacherEmployeeNo;

    @Schema(description = "所属企业ID")
    private String enterpriseId;

    @Schema(description = "企业名称")
    private String enterpriseName;

    @Schema(description = "所属专业方向ID")
    private String directionId;

    @Schema(description = "专业方向名称")
    private String directionName;

    @Schema(description = "对应的高校教师 user_id（null 表示未配对）")
    private String univTeacherId;

    @Schema(description = "对应的高校教师姓名")
    private String univTeacherName;

    @Schema(description = "配对来源（DIRECTION-方向级 DIRECT-精确配对 null-未配对）")
    private String coverageSource;

    @Schema(description = "是否已覆盖")
    private boolean covered;
}

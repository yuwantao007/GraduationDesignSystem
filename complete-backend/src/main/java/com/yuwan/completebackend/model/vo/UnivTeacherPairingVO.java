package com.yuwan.completebackend.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 高校教师配对信息响应 VO
 * <p>
 * 展示高校教师在 teacher_relationship 表中与企业教师的配对关系，
 * 与选报数据无关，配对存在即可查到。
 * </p>
 *
 * @author 系统架构师
 * @version 1.0
 * @since 2026-03-14
 */
@Data
@Schema(description = "高校教师配对信息")
public class UnivTeacherPairingVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "配对关系ID")
    private String relationId;

    @Schema(description = "企业教师用户ID")
    private String enterpriseTeacherId;

    @Schema(description = "企业教师姓名")
    private String enterpriseTeacherName;

    @Schema(description = "企业教师工号")
    private String enterpriseTeacherEmployeeNo;

    @Schema(description = "企业ID")
    private String enterpriseId;

    @Schema(description = "企业名称")
    private String enterpriseName;

    @Schema(description = "专业方向ID")
    private String directionId;

    @Schema(description = "专业方向名称")
    private String directionName;

    @Schema(description = "届别")
    private String cohort;

    @Schema(description = "配对类型（DIRECT-直接配对 ASSIST-辅助支持）")
    private String relationType;

    @Schema(description = "配对类型描述")
    private String relationTypeDesc;

    @Schema(description = "是否启用（1-启用 0-停用）")
    private Integer isEnabled;

    @Schema(description = "该企业教师的终审通过课题数")
    private Integer approvedTopicCount;

    @Schema(description = "该企业教师名下已有学生选报的课题数")
    private Integer selectionTopicCount;

    @Schema(description = "该企业教师名下中选学生总数")
    private Integer confirmedStudentCount;
}

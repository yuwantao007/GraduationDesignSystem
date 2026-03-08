package com.yuwan.completebackend.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 高校教师-企业教师精确配对表单参数
 *
 * @author 系统架构师
 * @version 1.0
 * @since 2026-03-08
 */
@Data
@Schema(description = "精确配对表单参数")
public class TeacherRelationDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotBlank(message = "高校教师不能为空")
    @Schema(description = "高校教师 user_id", requiredMode = Schema.RequiredMode.REQUIRED)
    private String univTeacherId;

    @NotBlank(message = "企业教师不能为空")
    @Schema(description = "企业教师 user_id", requiredMode = Schema.RequiredMode.REQUIRED)
    private String enterpriseTeacherId;

    @NotBlank(message = "企业不能为空")
    @Schema(description = "企业ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private String enterpriseId;

    @Schema(description = "所属专业方向（可选）")
    private String directionId;

    @NotBlank(message = "届别不能为空")
    @Schema(description = "届别", requiredMode = Schema.RequiredMode.REQUIRED)
    private String cohort;

    @Schema(description = "配对类型（DIRECT-直接配对 ASSIST-辅助支持）", defaultValue = "DIRECT")
    private String relationType;

    @Schema(description = "备注")
    private String remark;
}

package com.yuwan.completebackend.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 高校教师-专业方向分配表单参数
 *
 * @author 系统架构师
 * @version 1.0
 * @since 2026-03-08
 */
@Data
@Schema(description = "方向级分配表单参数")
public class UnivTeacherMajorDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotBlank(message = "高校教师不能为空")
    @Schema(description = "高校教师 user_id", requiredMode = Schema.RequiredMode.REQUIRED)
    private String univTeacherId;

    @NotBlank(message = "专业方向不能为空")
    @Schema(description = "专业方向ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private String directionId;

    @NotBlank(message = "所属企业不能为空")
    @Schema(description = "所属企业ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private String enterpriseId;

    @NotBlank(message = "届别不能为空")
    @Schema(description = "届别（如 2026届）", requiredMode = Schema.RequiredMode.REQUIRED)
    private String cohort;

    @Schema(description = "备注")
    private String remark;
}

package com.yuwan.completebackend.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 指派教师请求DTO
 * 企业负责人为校外协同开发课题中选学生指派企业指导教师
 *
 * @author 系统架构师
 * @version 1.0
 * @since 2026-03-14
 */
@Data
@Schema(description = "指派教师请求参数")
public class AssignTeacherDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "学生用户ID（可选，为空时由后端从 selectionId 推导）")
    private String studentId;

    @Schema(description = "课题ID（可选，为空时由后端从 selectionId 推导）")
    private String topicId;

    @NotBlank(message = "选报记录ID不能为空")
    @Schema(description = "关联选报记录ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private String selectionId;

    @NotBlank(message = "指派教师ID不能为空")
    @Schema(description = "指派教师ID（企业教师）", requiredMode = Schema.RequiredMode.REQUIRED)
    private String assignedTeacherId;
}

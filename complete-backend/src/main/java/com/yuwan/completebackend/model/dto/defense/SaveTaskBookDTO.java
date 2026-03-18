package com.yuwan.completebackend.model.dto.defense;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 保存任务书DTO
 *
 * @author 系统架构师
 * @version 1.0
 * @since 2026-03-17
 */
@Data
@Schema(description = "保存任务书请求对象")
public class SaveTaskBookDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotBlank(message = "学生ID不能为空")
    @Schema(description = "学生ID")
    private String studentId;

    @NotBlank(message = "课题ID不能为空")
    @Schema(description = "课题ID")
    private String topicId;

    @Schema(description = "任务书正文（富文本HTML）")
    private String content;

    @Schema(description = "任务书附件ID（关联document_info）")
    private String documentId;
}

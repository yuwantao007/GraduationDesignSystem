package com.yuwan.completebackend.model.dto.defense;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 提交开题报告DTO
 *
 * @author 系统架构师
 * @version 1.0
 * @since 2026-03-17
 */
@Data
@Schema(description = "提交开题报告请求对象")
public class SubmitReportDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotBlank(message = "课题ID不能为空")
    @Schema(description = "课题ID")
    private String topicId;

    @Schema(description = "对应答辩安排ID")
    private String arrangementId;

    @NotBlank(message = "报告文件ID不能为空")
    @Schema(description = "报告文件ID（关联document_info）")
    private String documentId;
}

package com.yuwan.completebackend.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 课题选报请求参数
 *
 * @author 系统架构师
 * @version 1.0
 * @since 2026-03-08
 */
@Data
@Schema(description = "课题选报请求参数")
public class ApplyTopicDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotBlank(message = "课题ID不能为空")
    @Schema(description = "课题ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private String topicId;

    @NotBlank(message = "选报理由不能为空")
    @Size(max = 500, message = "选报理由不能超过500字")
    @Schema(description = "选报理由", requiredMode = Schema.RequiredMode.REQUIRED)
    private String selectionReason;
}

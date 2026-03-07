package com.yuwan.completebackend.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 阶段切换请求DTO
 * 管理员切换系统阶段时使用
 *
 * @author 系统架构师
 * @version 1.0
 * @since 2026-03-07
 */
@Data
@Schema(description = "阶段切换请求参数")
public class SwitchPhaseDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotBlank(message = "目标阶段代码不能为空")
    @Schema(description = "目标阶段代码（如 TOPIC_SELECTION）", requiredMode = Schema.RequiredMode.REQUIRED)
    private String targetPhaseCode;

    @NotBlank(message = "切换原因不能为空")
    @Schema(description = "切换原因/备注", requiredMode = Schema.RequiredMode.REQUIRED)
    private String switchReason;
}

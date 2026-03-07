package com.yuwan.completebackend.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 初始化阶段请求DTO
 * 管理员首次启动系统阶段时使用
 *
 * @author 系统架构师
 * @version 1.0
 * @since 2026-03-07
 */
@Data
@Schema(description = "初始化阶段请求参数")
public class InitPhaseDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotBlank(message = "学届不能为空")
    @Schema(description = "毕业届别，如：2026届")
    private String cohort;

    @Schema(description = "初始化原因/备注")
    private String reason;
}

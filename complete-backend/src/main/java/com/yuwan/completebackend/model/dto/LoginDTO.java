package com.yuwan.completebackend.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 用户登录请求DTO
 *
 * @author 系统架构师
 * @version 1.0
 * @since 2026-02-20
 */
@Data
@Schema(description = "用户登录请求参数")
public class LoginDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotBlank(message = "学号/工号不能为空")
    @Schema(description = "登录标识（学号或工号）", requiredMode = Schema.RequiredMode.REQUIRED)
    private String identifier;

    @NotBlank(message = "登录密码不能为空")
    @Schema(description = "登录密码", requiredMode = Schema.RequiredMode.REQUIRED)
    private String password;
}

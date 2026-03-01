package com.yuwan.completebackend.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 创建角色请求DTO
 *
 * @author 系统架构师
 * @version 1.0
 * @since 2026-02-24
 */
@Data
@Schema(description = "创建角色请求")
public class CreateRoleDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotBlank(message = "角色名称不能为空")
    @Size(max = 50, message = "角色名称不能超过50字符")
    @Schema(description = "角色名称", required = true)
    private String roleName;

    @NotBlank(message = "角色代码不能为空")
    @Size(max = 50, message = "角色代码不能超过50字符")
    @Schema(description = "角色代码", required = true)
    private String roleCode;

    @Size(max = 200, message = "角色描述不能超过200字符")
    @Schema(description = "角色描述")
    private String description;

    @Schema(description = "权限ID列表")
    private List<String> permissionIds;
}

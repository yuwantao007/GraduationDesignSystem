package com.yuwan.completebackend.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 更新角色请求DTO
 *
 * @author 系统架构师
 * @version 1.0
 * @since 2026-02-24
 */
@Data
@Schema(description = "更新角色请求")
public class UpdateRoleDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Size(max = 50, message = "角色名称不能超过50字符")
    @Schema(description = "角色名称")
    private String roleName;

    @Size(max = 200, message = "角色描述不能超过200字符")
    @Schema(description = "角色描述")
    private String description;

    @Schema(description = "权限ID列表")
    private List<String> permissionIds;
}

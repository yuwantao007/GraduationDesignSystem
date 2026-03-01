package com.yuwan.completebackend.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 权限信息视图对象
 * 用于返回权限树形结构
 *
 * @author 系统架构师
 * @version 1.0
 * @since 2026-02-24
 */
@Data
@Schema(description = "权限信息VO")
public class PermissionVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "权限ID")
    private String permissionId;

    @Schema(description = "父级权限ID")
    private String parentId;

    @Schema(description = "权限名称")
    private String permissionName;

    @Schema(description = "权限编码")
    private String permissionCode;

    @Schema(description = "权限类型（1-菜单 2-按钮 3-接口）")
    private Integer permissionType;

    @Schema(description = "路由路径")
    private String path;

    @Schema(description = "图标")
    private String icon;

    @Schema(description = "排序号")
    private Integer sortOrder;

    @Schema(description = "权限状态（0-禁用 1-正常）")
    private Integer permissionStatus;

    @Schema(description = "子权限列表")
    private List<PermissionVO> children = new ArrayList<>();
}

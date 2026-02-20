package com.yuwan.completebackend.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * 权限信息实体类
 * 对应数据库表 permission_info
 *
 * @author 系统架构师
 * @version 1.0
 * @since 2026-02-20
 */
@Data
@TableName("permission_info")
@Schema(description = "权限信息实体")
public class Permission implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "permission_id", type = IdType.ASSIGN_ID)
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

    @TableLogic
    @Schema(description = "逻辑删除（0-未删除 1-已删除）")
    private Integer deleted;

    @TableField(fill = FieldFill.INSERT)
    @Schema(description = "创建时间")
    private Date createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    @Schema(description = "更新时间")
    private Date updateTime;
}

package com.yuwan.completebackend.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * 角色权限关联实体类
 * 对应数据库表 role_permission
 *
 * @author 系统架构师
 * @version 1.0
 * @since 2026-02-20
 */
@Data
@TableName("role_permission")
@Schema(description = "角色权限关联实体")
public class RolePermission implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    @Schema(description = "主键ID")
    private String id;

    @Schema(description = "角色ID")
    private String roleId;

    @Schema(description = "权限ID")
    private String permissionId;

    @Schema(description = "创建时间")
    private Date createTime;
}

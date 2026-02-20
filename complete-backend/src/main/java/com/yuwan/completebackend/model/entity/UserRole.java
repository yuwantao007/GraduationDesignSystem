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
 * 用户角色关联实体类
 * 对应数据库表 user_role
 *
 * @author 系统架构师
 * @version 1.0
 * @since 2026-02-20
 */
@Data
@TableName("user_role")
@Schema(description = "用户角色关联实体")
public class UserRole implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    @Schema(description = "主键ID")
    private String id;

    @Schema(description = "用户ID")
    private String userId;

    @Schema(description = "角色ID")
    private String roleId;

    @Schema(description = "创建时间")
    private Date createTime;
}

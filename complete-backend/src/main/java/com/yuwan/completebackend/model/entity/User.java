package com.yuwan.completebackend.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * 用户信息实体类
 * 对应数据库表 user_info
 *
 * @author 系统架构师
 * @version 1.0
 * @since 2026-02-20
 */
@Data
@TableName("user_info")
@Schema(description = "用户信息实体")
public class User implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "user_id", type = IdType.ASSIGN_ID)
    @Schema(description = "用户ID")
    private String userId;

    @Schema(description = "登录账号")
    private String username;

    @Schema(description = "登录密码")
    private String password;

    @Schema(description = "真实姓名")
    private String realName;

    @Schema(description = "用户邮箱")
    private String userEmail;

    @Schema(description = "手机号码")
    private String userPhone;

    @Schema(description = "头像地址")
    private String avatar;

    @Schema(description = "性别（0-女 1-男）")
    private Integer gender;

    @Schema(description = "所属院系/企业")
    private String department;

    @Schema(description = "专业方向（文本）")
    private String major;

    @Schema(description = "所属专业方向ID")
    private String directionId;

    @Schema(description = "所在专业ID（学生精确专业关联）")
    private String majorId;

    @Schema(description = "学号")
    private String studentNo;

    @Schema(description = "工号")
    private String employeeNo;

    @Schema(description = "职称")
    private String title;

    @Schema(description = "账号状态（0-禁用 1-正常 2-锁定）")
    private Integer userStatus;

    @Schema(description = "最后登录时间")
    private Date lastLoginTime;

    @Schema(description = "最后登录IP")
    private String lastLoginIp;

    @Schema(description = "备注")
    private String remark;

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

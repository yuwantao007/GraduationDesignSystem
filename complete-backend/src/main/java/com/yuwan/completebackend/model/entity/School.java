package com.yuwan.completebackend.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * 学校信息实体类
 * 对应数据库表 school_info
 *
 * @author 系统架构师
 * @version 1.0
 * @since 2026-02-22
 */
@Data
@TableName("school_info")
@Schema(description = "学校信息实体")
public class School implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "school_id", type = IdType.ASSIGN_ID)
    @Schema(description = "学校ID")
    private String schoolId;

    @Schema(description = "学校名称")
    private String schoolName;

    @Schema(description = "学校编码")
    private String schoolCode;

    @Schema(description = "详细地址")
    private String address;

    @Schema(description = "联系人")
    private String contactPerson;

    @Schema(description = "联系电话")
    private String contactPhone;

    @Schema(description = "学校邮箱")
    private String schoolEmail;

    @Schema(description = "学校简介")
    private String description;

    @Schema(description = "状态（0-禁用 1-正常）")
    private Integer schoolStatus;

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

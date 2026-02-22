package com.yuwan.completebackend.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * 企业信息实体类
 * 对应数据库表 enterprise_info
 *
 * @author 系统架构师
 * @version 1.0
 * @since 2026-02-21
 */
@Data
@TableName("enterprise_info")
@Schema(description = "企业信息实体")
public class Enterprise implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "enterprise_id", type = IdType.ASSIGN_ID)
    @Schema(description = "企业ID")
    private String enterpriseId;

    @Schema(description = "企业名称")
    private String enterpriseName;

    @Schema(description = "企业编码")
    private String enterpriseCode;

    @Schema(description = "联系人")
    private String contactPerson;

    @Schema(description = "联系电话")
    private String contactPhone;

    @Schema(description = "联系邮箱")
    private String contactEmail;

    @Schema(description = "企业地址")
    private String address;

    @Schema(description = "企业简介")
    private String description;

    @Schema(description = "状态（0-禁用 1-正常）")
    private Integer enterpriseStatus;

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

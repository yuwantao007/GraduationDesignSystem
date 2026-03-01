package com.yuwan.completebackend.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * 专业方向实体类
 * 对应数据库表 major_direction_info
 * <p>
 * 专业方向是专业的一级分类，如"计算机科学与技术"、"软件工程"等
 * </p>
 *
 * @author 系统架构师
 * @version 1.0
 * @since 2026-03-01
 */
@Data
@TableName("major_direction_info")
@Schema(description = "专业方向实体")
public class MajorDirection implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "direction_id", type = IdType.ASSIGN_ID)
    @Schema(description = "专业方向ID")
    private String directionId;

    @Schema(description = "所属企业ID")
    private String enterpriseId;

    @Schema(description = "专业方向名称")
    private String directionName;

    @Schema(description = "专业方向代码")
    private String directionCode;

    @Schema(description = "专业方向描述")
    private String description;

    @Schema(description = "方向负责人ID")
    private String leaderId;

    @Schema(description = "方向负责人姓名")
    private String leaderName;

    @Schema(description = "排序")
    private Integer sortOrder;

    @Schema(description = "状态（1-启用 0-禁用）")
    private Integer status;

    @TableLogic
    @Schema(description = "删除标记（0-未删除 1-已删除）")
    private Integer deleted;

    @TableField(fill = FieldFill.INSERT)
    @Schema(description = "创建时间")
    private Date createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    @Schema(description = "更新时间")
    private Date updateTime;
}

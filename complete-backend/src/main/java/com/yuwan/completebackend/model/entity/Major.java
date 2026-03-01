package com.yuwan.completebackend.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * 专业实体类
 * 对应数据库表 major_info
 * <p>
 * 专业是专业方向的二级分类，如"人工智能"、"大数据技术"等
 * </p>
 *
 * @author 系统架构师
 * @version 1.0
 * @since 2026-03-01
 */
@Data
@TableName("major_info")
@Schema(description = "专业实体")
public class Major implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "major_id", type = IdType.ASSIGN_ID)
    @Schema(description = "专业ID")
    private String majorId;

    @Schema(description = "所属专业方向ID")
    private String directionId;

    @Schema(description = "所属企业ID")
    private String enterpriseId;

    @Schema(description = "专业名称")
    private String majorName;

    @Schema(description = "专业代码")
    private String majorCode;

    @Schema(description = "学位类型（本科/专科）")
    private String degreeType;

    @Schema(description = "学制（年）")
    private Integer educationYears;

    @Schema(description = "专业描述")
    private String description;

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

package com.yuwan.completebackend.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * 高校教师-企业教师精确配对实体类
 * 对应数据库表 teacher_relationship
 * <p>
 * 细粒度配对：特殊情况下高校教师与企业教师的精确配对，
 * 优先级高于专业方向级别的分配。
 * </p>
 *
 * @author 系统架构师
 * @version 1.0
 * @since 2026-03-08
 */
@Data
@TableName("teacher_relationship")
@Schema(description = "高校教师-企业教师精确配对实体")
public class TeacherRelationship implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "relation_id", type = IdType.ASSIGN_ID)
    @Schema(description = "主键ID")
    private String relationId;

    @Schema(description = "高校教师 user_id")
    private String univTeacherId;

    @Schema(description = "企业教师 user_id")
    private String enterpriseTeacherId;

    @Schema(description = "企业ID")
    private String enterpriseId;

    @Schema(description = "所属专业方向（辅助描述，可选）")
    private String directionId;

    @Schema(description = "届别")
    private String cohort;

    @Schema(description = "配对类型（DIRECT-直接配对 ASSIST-辅助支持）")
    private String relationType;

    @Schema(description = "是否启用（1-启用 0-停用）")
    private Integer isEnabled;

    @Schema(description = "备注")
    private String remark;

    @TableLogic
    @Schema(description = "逻辑删除（0-未删除 1-已删除）")
    private Integer deleted;

    @Schema(description = "创建时间")
    private Date createTime;

    @Schema(description = "更新时间")
    private Date updateTime;
}

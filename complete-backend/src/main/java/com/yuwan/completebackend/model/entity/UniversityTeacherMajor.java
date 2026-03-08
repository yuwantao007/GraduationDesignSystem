package com.yuwan.completebackend.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * 高校教师-专业方向分配实体类
 * 对应数据库表 university_teacher_major
 * <p>
 * 粗粒度分配：一个高校教师对接一个企业的某专业方向，
 * 该方向下所有企业教师默认由该高校教师负责预审、论文指导等。
 * </p>
 *
 * @author 系统架构师
 * @version 1.0
 * @since 2026-03-08
 */
@Data
@TableName("university_teacher_major")
@Schema(description = "高校教师-专业方向分配实体")
public class UniversityTeacherMajor implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    @Schema(description = "主键ID")
    private String id;

    @Schema(description = "高校教师 user_id")
    private String univTeacherId;

    @Schema(description = "专业方向ID")
    private String directionId;

    @Schema(description = "所属企业ID")
    private String enterpriseId;

    @Schema(description = "届别（如 2026届）")
    private String cohort;

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

package com.yuwan.completebackend.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * 教师指派记录实体类
 * 对应数据库表 teacher_assignment
 * <p>
 * 企业负责人为"校外协同开发课题"中选学生指派企业指导教师
 * </p>
 *
 * @author 系统架构师
 * @version 1.0
 * @since 2026-03-14
 */
@Data
@TableName("teacher_assignment")
@Schema(description = "教师指派记录实体")
public class TeacherAssignment implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "assignment_id", type = IdType.ASSIGN_ID)
    @Schema(description = "指派ID（雪花ID）")
    private String assignmentId;

    @Schema(description = "学生用户ID")
    private String studentId;

    @Schema(description = "课题ID")
    private String topicId;

    @Schema(description = "关联选报记录ID")
    private String selectionId;

    @Schema(description = "指派教师ID（企业教师）")
    private String assignedTeacherId;

    @Schema(description = "指派人ID（企业负责人）")
    private String assignedBy;

    @Schema(description = "指派时间")
    private Date assignTime;

    @Schema(description = "取消指派时间")
    private Date cancelTime;

    @Schema(description = "指派状态（1-已指派 0-已取消）")
    private Integer assignStatus;

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

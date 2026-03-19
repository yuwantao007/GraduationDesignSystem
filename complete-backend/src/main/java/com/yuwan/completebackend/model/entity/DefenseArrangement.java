package com.yuwan.completebackend.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 答辩安排实体类
 * 对应数据库表 defense_arrangement
 *
 * @author 系统架构师
 * @version 1.0
 * @since 2026-03-17
 */
@Data
@TableName(value = "defense_arrangement", autoResultMap = true)
@Schema(description = "答辩安排实体")
public class DefenseArrangement implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "arrangement_id", type = IdType.ASSIGN_ID)
    @Schema(description = "安排ID")
    private String arrangementId;

    @Schema(description = "答辩类型: 1=开题, 2=中期, 3=正式, 4=二次")
    private Integer defenseType;

    @Schema(description = "课题类别（高职升本/3+1/实验班）")
    private String topicCategory;

    @Schema(description = "专业ID")
    private String majorId;

    @Schema(description = "答辩时间")
    private Date defenseTime;

    @Schema(description = "答辩地点")
    private String defenseLocation;

    @TableField(typeHandler = JacksonTypeHandler.class)
    @Schema(description = "答辩小组教师ID列表（JSON数组）")
    private List<String> panelTeachers;

    @Schema(description = "报告提交截止时间")
    private Date deadline;

    @Schema(description = "毕业届别（如：2026届）")
    private String cohort;

    @Schema(description = "所属企业ID")
    private String enterpriseId;

    @Schema(description = "创建人ID（企业负责人）")
    private String creatorId;

    @Schema(description = "状态: 1=启用, 0=禁用")
    private Integer status;

    @Schema(description = "备注说明")
    private String remark;

    @TableLogic
    @TableField("is_deleted")
    @Schema(description = "逻辑删除: 0=正常, 1=删除")
    private Integer deleted;

    @TableField(fill = FieldFill.INSERT)
    @Schema(description = "创建时间")
    private Date createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    @Schema(description = "更新时间")
    private Date updateTime;
}


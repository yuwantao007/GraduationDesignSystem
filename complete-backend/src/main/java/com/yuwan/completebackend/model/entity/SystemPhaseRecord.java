package com.yuwan.completebackend.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * 系统阶段切换记录实体类
 * 对应数据库表 system_phase_record
 * 
 * <p>用于记录每次阶段切换的操作信息，支持审计追溯</p>
 *
 * @author 系统架构师
 * @version 1.0
 * @since 2026-03-07
 */
@Data
@TableName("system_phase_record")
@Schema(description = "系统阶段切换记录实体")
public class SystemPhaseRecord implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "record_id", type = IdType.ASSIGN_ID)
    @Schema(description = "记录ID")
    private String recordId;

    @Schema(description = "毕业届别（如：2026届）")
    private String cohort;

    @Schema(description = "当前阶段代码")
    private String phaseCode;

    @Schema(description = "当前阶段序号")
    private Integer phaseOrder;

    @Schema(description = "前一阶段代码（首个阶段为NULL）")
    private String previousPhaseCode;

    @Schema(description = "切换时间")
    private Date switchTime;

    @Schema(description = "操作人ID（管理员）")
    private String operatorId;

    @Schema(description = "操作人姓名")
    private String operatorName;

    @Schema(description = "切换原因/备注")
    private String switchReason;

    @Schema(description = "是否为当前生效记录（0-否 1-是）")
    private Integer isCurrent;

    @TableField(fill = FieldFill.INSERT)
    @Schema(description = "创建时间")
    private Date createTime;
}

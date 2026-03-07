package com.yuwan.completebackend.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * 系统阶段配置实体类
 * 对应数据库表 system_phase_config
 * 
 * <p>固定4条记录，只读配置表，不允许动态增删</p>
 *
 * @author 系统架构师
 * @version 1.0
 * @since 2026-03-07
 */
@Data
@TableName("system_phase_config")
@Schema(description = "系统阶段配置实体")
public class SystemPhaseConfig implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "phase_id", type = IdType.AUTO)
    @Schema(description = "阶段主键")
    private Integer phaseId;

    @Schema(description = "阶段代码")
    private String phaseCode;

    @Schema(description = "阶段中文名")
    private String phaseName;

    @Schema(description = "阶段序号（1/2/3/4）")
    private Integer phaseOrder;

    @Schema(description = "阶段描述")
    private String phaseDescription;

    @Schema(description = "前端展示图标")
    private String phaseIcon;

    @Schema(description = "前端展示颜色")
    private String phaseColor;

    @TableLogic
    @Schema(description = "逻辑删除（0-未删除 1-已删除）")
    private Integer isDeleted;

    @Schema(description = "创建时间")
    private Date createTime;
}

package com.yuwan.completebackend.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * 系统预警记录实体类
 * 对应数据库表 alert_info
 * <p>
 * 由定时任务自动生成，记录系统运行过程中检测到的各类预警事件，
 * 供系统管理员和企业负责人查看与处理。
 * </p>
 *
 * @author 系统架构师
 * @version 1.0
 * @since 2026-03-14
 */
@Data
@TableName("alert_info")
@Schema(description = "系统预警记录实体")
public class Alert implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "alert_id", type = IdType.ASSIGN_ID)
    @Schema(description = "预警ID")
    private String alertId;

    @Schema(description = "预警类型（枚举值：STUDENT_NOT_SELECTED/TOPIC_NO_APPLICANT/REVIEW_BACKLOG/PHASE_DEADLINE_NEAR/SELECTION_RATE_LOW）")
    private String alertType;

    @Schema(description = "预警级别（1-提示 2-警告 3-严重）")
    private Integer alertLevel;

    @Schema(description = "预警标题")
    private String alertTitle;

    @Schema(description = "预警详情内容")
    private String alertContent;

    @Schema(description = "关联对象ID（课题ID / 学生ID / 阶段代码）")
    private String targetId;

    @Schema(description = "关联对象类型（TOPIC / STUDENT / PHASE）")
    private String targetType;

    @Schema(description = "是否已读（0-未读 1-已读）")
    private Integer isRead;

    @Schema(description = "是否已处理（0-未处理 1-已处理）")
    private Integer isResolved;

    @Schema(description = "处理时间")
    private Date resolvedAt;

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

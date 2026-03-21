package com.yuwan.completebackend.model.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * 站内消息操作日志实体
 * 对应数据库表 notification_action_log
 *
 * @author 系统架构师
 * @version 1.0
 * @since 2026-03-21
 */
@Data
@TableName("notification_action_log")
@Schema(description = "站内消息操作日志实体")
public class NotificationActionLog implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "log_id", type = IdType.ASSIGN_ID)
    @Schema(description = "日志ID")
    private String logId;

    @Schema(description = "消息ID")
    private String messageId;

    @Schema(description = "操作用户ID")
    private String operatorId;

    @Schema(description = "操作类型（READ/READ_ALL/PROCESS/DELETE）")
    private String actionType;

    @Schema(description = "操作来源（USER/SYSTEM）")
    private String actionSource;

    @TableLogic
    @TableField("is_deleted")
    @Schema(description = "逻辑删除（0-未删除 1-已删除）")
    private Integer deleted;

    @TableField(fill = FieldFill.INSERT)
    @Schema(description = "创建时间")
    private Date createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    @Schema(description = "更新时间")
    private Date updateTime;
}

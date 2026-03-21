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
 * 站内消息实体
 * 对应数据库表 notification_message
 *
 * @author 系统架构师
 * @version 1.0
 * @since 2026-03-21
 */
@Data
@TableName("notification_message")
@Schema(description = "站内消息实体")
public class NotificationMessage implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "message_id", type = IdType.ASSIGN_ID)
    @Schema(description = "消息ID")
    private String messageId;

    @Schema(description = "接收人ID")
    private String receiverId;

    @Schema(description = "消息分类")
    private String category;

    @Schema(description = "消息级别（1-普通 2-提醒 3-重要 4-紧急）")
    private Integer level;

    @Schema(description = "消息标题")
    private String title;

    @Schema(description = "消息内容")
    private String content;

    @Schema(description = "业务类型")
    private String businessType;

    @Schema(description = "业务ID")
    private String businessId;

    @Schema(description = "业务跳转路由")
    private String businessRoute;

    @Schema(description = "去重键")
    private String dedupKey;

    @Schema(description = "消息状态（0-未读 1-已读 2-已处理）")
    private Integer messageStatus;

    @Schema(description = "已读时间")
    private Date readTime;

    @Schema(description = "处理时间")
    private Date processedTime;

    @Schema(description = "过期时间")
    private Date expireTime;

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

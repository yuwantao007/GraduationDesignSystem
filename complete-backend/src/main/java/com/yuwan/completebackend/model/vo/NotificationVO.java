package com.yuwan.completebackend.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * 站内消息响应VO
 *
 * @author 系统架构师
 * @version 1.0
 * @since 2026-03-21
 */
@Data
@Schema(description = "站内消息响应")
public class NotificationVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "消息ID")
    private String messageId;

    @Schema(description = "接收人ID")
    private String receiverId;

    @Schema(description = "消息分类")
    private String category;

    @Schema(description = "消息分类描述")
    private String categoryDesc;

    @Schema(description = "消息级别")
    private Integer level;

    @Schema(description = "消息级别描述")
    private String levelDesc;

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

    @Schema(description = "消息状态（0-未读 1-已读 2-已处理）")
    private Integer messageStatus;

    @Schema(description = "已读时间")
    private Date readTime;

    @Schema(description = "处理时间")
    private Date processedTime;

    @Schema(description = "过期时间")
    private Date expireTime;

    @Schema(description = "创建时间")
    private Date createTime;
}

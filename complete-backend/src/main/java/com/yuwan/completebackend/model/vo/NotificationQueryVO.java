package com.yuwan.completebackend.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 站内消息查询参数VO
 *
 * @author 系统架构师
 * @version 1.0
 * @since 2026-03-21
 */
@Data
@Schema(description = "站内消息查询参数")
public class NotificationQueryVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "消息分类")
    private String category;

    @Schema(description = "消息级别")
    private Integer level;

    @Schema(description = "消息状态（0-未读 1-已读 2-已处理）")
    private Integer status;

    @Schema(description = "关键字（标题/内容）")
    private String keyword;

    @Schema(description = "页码")
    private Integer pageNum = 1;

    @Schema(description = "每页大小")
    private Integer pageSize = 20;
}

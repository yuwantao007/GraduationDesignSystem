package com.yuwan.completebackend.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;

/**
 * 系统预警记录 VO
 * <p>面向前端展示的预警信息，包含全部可读字段</p>
 *
 * @author 系统架构师
 * @version 1.0
 * @since 2026-03-14
 */
@Data
@Schema(description = "系统预警记录展示信息")
public class AlertVO {

    @Schema(description = "预警ID")
    private String alertId;

    @Schema(description = "预警类型代码")
    private String alertType;

    @Schema(description = "预警类型描述")
    private String alertTypeDesc;

    @Schema(description = "预警级别（1-提示 2-警告 3-严重）")
    private Integer alertLevel;

    @Schema(description = "预警级别描述")
    private String alertLevelDesc;

    @Schema(description = "预警标题")
    private String alertTitle;

    @Schema(description = "预警详情内容")
    private String alertContent;

    @Schema(description = "关联对象ID")
    private String targetId;

    @Schema(description = "关联对象类型")
    private String targetType;

    @Schema(description = "是否已读（0-未读 1-已读）")
    private Integer isRead;

    @Schema(description = "是否已处理（0-未处理 1-已处理）")
    private Integer isResolved;

    @Schema(description = "处理时间")
    private Date resolvedAt;

    @Schema(description = "创建时间")
    private Date createTime;

    @Schema(description = "更新时间")
    private Date updateTime;
}

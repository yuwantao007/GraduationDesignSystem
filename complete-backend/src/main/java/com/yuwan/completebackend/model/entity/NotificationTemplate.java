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
 * 站内消息模板实体
 * 对应数据库表 notification_template
 *
 * @author 系统架构师
 * @version 1.0
 * @since 2026-03-21
 */
@Data
@TableName("notification_template")
@Schema(description = "站内消息模板实体")
public class NotificationTemplate implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "template_id", type = IdType.ASSIGN_ID)
    @Schema(description = "模板ID")
    private String templateId;

    @Schema(description = "模板编码")
    private String templateCode;

    @Schema(description = "模板名称")
    private String templateName;

    @Schema(description = "消息分类")
    private String category;

    @Schema(description = "默认消息级别")
    private Integer defaultLevel;

    @Schema(description = "标题模板")
    private String titleTemplate;

    @Schema(description = "内容模板")
    private String contentTemplate;

    @Schema(description = "是否启用（1-启用 0-禁用）")
    private Integer isEnabled;

    @Schema(description = "备注")
    private String remark;

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

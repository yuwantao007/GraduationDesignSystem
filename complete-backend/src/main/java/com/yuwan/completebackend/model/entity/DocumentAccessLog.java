package com.yuwan.completebackend.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * 文档访问日志实体类
 * 对应数据库表 document_access_log
 *
 * @author 系统架构师
 * @version 1.0
 * @since 2026-03-15
 */
@Data
@TableName("document_access_log")
@Schema(description = "文档访问日志实体")
public class DocumentAccessLog implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "log_id", type = IdType.ASSIGN_ID)
    @Schema(description = "日志ID")
    private String logId;

    @Schema(description = "文档ID")
    private String documentId;

    @Schema(description = "访问人ID")
    private String accessorId;

    @Schema(description = "访问类型（1-预览 2-下载）")
    private Integer accessType;

    @Schema(description = "访问时间")
    private Date accessTime;

    @Schema(description = "访问IP地址")
    private String ipAddress;
}

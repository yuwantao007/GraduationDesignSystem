package com.yuwan.completebackend.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 文档预览VO
 *
 * @author 系统架构师
 * @version 1.0
 * @since 2026-03-15
 */
@Data
@Schema(description = "文档预览返回对象")
public class DocumentPreviewVO {

    @Schema(description = "文档ID")
    private String documentId;

    @Schema(description = "原始文件名")
    private String fileName;

    @Schema(description = "文件后缀")
    private String fileSuffix;

    @Schema(description = "预签名URL（有效期15分钟）")
    private String previewUrl;

    @Schema(description = "URL过期时间（秒）")
    private Integer expiresIn;

    @Schema(description = "是否支持在线预览")
    private Boolean supportPreview;
}

package com.yuwan.completebackend.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 文档查询条件VO
 *
 * @author 系统架构师
 * @version 1.0
 * @since 2026-03-15
 */
@Data
@Schema(description = "文档查询条件")
public class DocumentQueryVO {

    @Schema(description = "学生ID")
    private String studentId;

    @Schema(description = "课题ID")
    private String topicId;

    @Schema(description = "文档类型")
    private Integer documentType;

    @Schema(description = "是否只显示最新版本")
    private Boolean latestOnly;

    @Schema(description = "文件名关键字")
    private String keyword;

    @Schema(description = "页码")
    private Integer pageNum = 1;

    @Schema(description = "每页大小")
    private Integer pageSize = 10;
}

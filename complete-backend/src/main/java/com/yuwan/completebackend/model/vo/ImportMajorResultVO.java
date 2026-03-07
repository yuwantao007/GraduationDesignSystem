package com.yuwan.completebackend.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * Excel 导入专业结果 VO
 */
@Data
@Schema(description = "Excel导入专业结果")
public class ImportMajorResultVO {

    @Schema(description = "总数据行数（不含表头）")
    private int totalCount;

    @Schema(description = "成功创建的专业方向数")
    private int directionCreatedCount;

    @Schema(description = "成功创建的专业数")
    private int majorCreatedCount;

    @Schema(description = "成功关联的教师数")
    private int teacherLinkedCount;

    @Schema(description = "跳过的行数（数据已存在）")
    private int skipCount;

    @Schema(description = "失败的行数")
    private int failureCount;

    @Schema(description = "错误明细列表")
    private List<ImportRowErrorVO> errors;

    /**
     * 导入错误行明细
     */
    @Data
    @Schema(description = "导入错误行明细")
    public static class ImportRowErrorVO {

        @Schema(description = "行号（从2开始，含表头为第1行）")
        private int rowNum;

        @Schema(description = "错误信息")
        private String errorMsg;
    }
}

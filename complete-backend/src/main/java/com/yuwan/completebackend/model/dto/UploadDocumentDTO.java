package com.yuwan.completebackend.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

/**
 * 上传文档DTO
 *
 * @author 系统架构师
 * @version 1.0
 * @since 2026-03-15
 */
@Data
@Schema(description = "上传文档请求参数")
public class UploadDocumentDTO {

    @NotNull(message = "文档类型不能为空")
    @Schema(description = "文档类型（1-项目代码 2-论文文档 3-开题报告 4-中期检查表 5-教师批注文档）")
    private Integer documentType;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "关联的学生ID（教师上传批注时必填）")
    private String studentId;
}

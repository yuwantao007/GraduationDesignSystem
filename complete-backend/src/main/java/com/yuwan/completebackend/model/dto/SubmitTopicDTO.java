package com.yuwan.completebackend.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 课题提交请求DTO
 * 企业教师提交课题申报时使用
 *
 * @author 系统架构师
 * @version 1.0
 * @since 2026-02-21
 */
@Data
@Schema(description = "课题提交请求参数")
public class SubmitTopicDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotBlank(message = "课题ID不能为空")
    @Schema(description = "课题ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private String topicId;
}

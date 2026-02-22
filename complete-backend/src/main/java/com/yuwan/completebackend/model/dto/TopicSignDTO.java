package com.yuwan.completebackend.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 课题签名请求DTO
 * 用于学院负责人、企业负责人、企业指导教师签名
 *
 * @author 系统架构师
 * @version 1.0
 * @since 2026-02-21
 */
@Data
@Schema(description = "课题签名请求参数")
public class TopicSignDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotBlank(message = "课题ID不能为空")
    @Schema(description = "课题ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private String topicId;

    @NotBlank(message = "签名图片不能为空")
    @Schema(description = "签名图片URL", requiredMode = Schema.RequiredMode.REQUIRED)
    private String signImage;

    @NotBlank(message = "签名类型不能为空")
    @Schema(description = "签名类型（college_leader-学院负责人 enterprise_leader-企业负责人 enterprise_teacher-企业指导教师）", 
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String signType;
}

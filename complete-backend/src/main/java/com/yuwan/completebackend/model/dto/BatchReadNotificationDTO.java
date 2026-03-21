package com.yuwan.completebackend.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 批量已读请求DTO
 *
 * @author 系统架构师
 * @version 1.0
 * @since 2026-03-21
 */
@Data
@Schema(description = "批量已读请求")
public class BatchReadNotificationDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotEmpty(message = "消息ID列表不能为空")
    @Schema(description = "消息ID列表")
    private List<String> messageIds;
}

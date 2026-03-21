package com.yuwan.completebackend.controller;

import com.yuwan.completebackend.common.PageResult;
import com.yuwan.completebackend.common.Result;
import com.yuwan.completebackend.model.dto.BatchReadNotificationDTO;
import com.yuwan.completebackend.model.vo.NotificationQueryVO;
import com.yuwan.completebackend.model.vo.NotificationVO;
import com.yuwan.completebackend.service.INotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 站内消息控制器
 *
 * @author 系统架构师
 * @version 1.0
 * @since 2026-03-21
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/notification")
@Tag(name = "站内消息", description = "站内消息查询、已读、处理与删除接口")
public class NotificationController {

    private final INotificationService notificationService;

    @GetMapping("/list")
    @PreAuthorize("hasAuthority('notification:view')")
    @Operation(summary = "我的消息列表", description = "分页查询当前用户的站内消息")
    public Result<PageResult<NotificationVO>> list(NotificationQueryVO queryVO) {
        return Result.success(notificationService.getMyNotifications(queryVO));
    }

    @GetMapping("/unread-count")
    @PreAuthorize("hasAuthority('notification:view')")
    @Operation(summary = "未读消息数", description = "获取当前用户未读消息数量")
    public Result<Long> unreadCount() {
        return Result.success(notificationService.getUnreadCount());
    }

    @GetMapping("/{messageId}")
    @PreAuthorize("hasAuthority('notification:view')")
    @Operation(summary = "消息详情", description = "查询单条消息详情")
    public Result<NotificationVO> detail(@Parameter(description = "消息ID") @PathVariable String messageId) {
        return Result.success(notificationService.getDetail(messageId));
    }

    @PostMapping("/{messageId}/read")
    @PreAuthorize("hasAuthority('notification:read')")
    @Operation(summary = "标记已读", description = "标记单条消息为已读")
    public Result<Void> markRead(@Parameter(description = "消息ID") @PathVariable String messageId) {
        notificationService.markAsRead(messageId);
        return Result.success();
    }

    @PostMapping("/read-batch")
    @PreAuthorize("hasAuthority('notification:read')")
    @Operation(summary = "批量已读", description = "批量标记消息为已读")
    public Result<Void> batchRead(@Valid @RequestBody BatchReadNotificationDTO dto) {
        notificationService.batchMarkAsRead(dto);
        return Result.success();
    }

    @PostMapping("/read-all")
    @PreAuthorize("hasAuthority('notification:read')")
    @Operation(summary = "全部已读", description = "当前用户全部消息标记为已读")
    public Result<Void> readAll() {
        notificationService.markAllAsRead();
        return Result.success();
    }

    @PostMapping("/{messageId}/process")
    @PreAuthorize("hasAuthority('notification:process')")
    @Operation(summary = "标记已处理", description = "标记消息为已处理状态")
    public Result<Void> process(@Parameter(description = "消息ID") @PathVariable String messageId) {
        notificationService.markAsProcessed(messageId);
        return Result.success();
    }

    @DeleteMapping("/{messageId}")
    @PreAuthorize("hasAuthority('notification:delete')")
    @Operation(summary = "删除消息", description = "逻辑删除当前用户消息")
    public Result<Void> delete(@Parameter(description = "消息ID") @PathVariable String messageId) {
        notificationService.deleteMessage(messageId);
        return Result.success();
    }
}

package com.yuwan.completebackend.controller;

import com.yuwan.completebackend.common.PageResult;
import com.yuwan.completebackend.common.Result;
import com.yuwan.completebackend.model.vo.AlertVO;
import com.yuwan.completebackend.service.IAlertService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 系统预警控制器
 * <p>提供预警记录的查询、标记已读、处理等接口</p>
 *
 * @author 系统架构师
 * @version 1.0
 * @since 2026-03-14
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/alert")
@Tag(name = "系统预警", description = "系统预警记录查询、标记已读、处理接口")
public class AlertController {

    private final IAlertService alertService;

    /**
     * 分页查询预警列表
     *
     * @param alertType  预警类型过滤（可选）
     * @param alertLevel 预警级别过滤（可选）
     * @param isRead     已读状态过滤（0-未读 1-已读，可选）
     * @param isResolved 处理状态过滤（0-未处理 1-已处理，可选）
     * @param page       页码（默认1）
     * @param size       每页条数（默认20）
     * @return 分页预警列表
     */
    @GetMapping("/list")
    @PreAuthorize("hasAuthority('monitor:alert:view')")
    @Operation(summary = "预警列表", description = "分页查询系统预警记录，支持类型/级别/已读/已处理多维度过滤")
    public Result<PageResult<AlertVO>> listAlerts(
            @Parameter(description = "预警类型") @RequestParam(required = false) String alertType,
            @Parameter(description = "预警级别（1-提示 2-警告 3-严重）") @RequestParam(required = false) Integer alertLevel,
            @Parameter(description = "是否已读（0-未读 1-已读）") @RequestParam(required = false) Integer isRead,
            @Parameter(description = "是否已处理（0-未处理 1-已处理）") @RequestParam(required = false) Integer isResolved,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页条数") @RequestParam(defaultValue = "20") int size) {
        return Result.success(alertService.listAlerts(alertType, alertLevel, isRead, isResolved, page, size));
    }

    /**
     * 获取未读预警数量（供导航栏徽标使用）
     *
     * @return 未读数量
     */
    @GetMapping("/unread-count")
    @PreAuthorize("hasAuthority('monitor:alert:view')")
    @Operation(summary = "未读预警数", description = "返回当前未读预警总数，用于导航栏徽标展示")
    public Result<Long> getUnreadCount() {
        return Result.success(alertService.getUnreadCount());
    }

    /**
     * 标记单条预警为已读
     *
     * @param id 预警ID
     */
    @PostMapping("/{id}/read")
    @PreAuthorize("hasAuthority('monitor:alert:view')")
    @Operation(summary = "标记已读", description = "将指定预警标记为已读状态")
    public Result<Void> markAsRead(@PathVariable String id) {
        alertService.markAsRead(id);
        return Result.success();
    }

    /**
     * 标记单条预警为已处理
     *
     * @param id 预警ID
     */
    @PostMapping("/{id}/resolve")
    @PreAuthorize("hasAuthority('monitor:alert:resolve')")
    @Operation(summary = "标记已处理", description = "将指定预警标记为已处理，同时自动标记为已读")
    public Result<Void> markAsResolved(@PathVariable String id) {
        log.info("标记预警已处理，alertId={}", id);
        alertService.markAsResolved(id);
        return Result.success();
    }

    /**
     * 全部标记为已读
     */
    @PostMapping("/read-all")
    @PreAuthorize("hasAuthority('monitor:alert:view')")
    @Operation(summary = "全部已读", description = "将所有未读预警标记为已读")
    public Result<Void> markAllAsRead() {
        alertService.markAllAsRead();
        return Result.success();
    }
}

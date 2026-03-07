package com.yuwan.completebackend.controller;

import com.yuwan.completebackend.common.Result;
import com.yuwan.completebackend.model.dto.InitPhaseDTO;
import com.yuwan.completebackend.model.dto.SwitchPhaseDTO;
import com.yuwan.completebackend.model.vo.PhaseRecordVO;
import com.yuwan.completebackend.model.vo.PhaseStatusVO;
import com.yuwan.completebackend.service.ISystemPhaseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 系统阶段管理控制器
 * 提供阶段查询、初始化、切换、历史记录等接口
 *
 * @author 系统架构师
 * @version 1.0
 * @since 2026-03-07
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/system/phase")
@Tag(name = "系统阶段管理", description = "系统阶段查询、初始化、切换、历史记录等接口")
public class SystemPhaseController {

    private final ISystemPhaseService systemPhaseService;

    /**
     * 获取当前系统阶段状态
     * 所有已认证用户均可访问
     *
     * @return 当前阶段状态（含进度条信息）
     */
    @GetMapping("/current")
    @Operation(summary = "获取当前阶段状态", description = "返回当前系统阶段信息，含进度条、各阶段状态列表")
    public Result<PhaseStatusVO> getCurrentPhaseStatus() {
        PhaseStatusVO status = systemPhaseService.getCurrentPhaseStatus();
        return Result.success(status);
    }

    /**
     * 初始化系统阶段
     * 仅系统管理员可操作，设置学期并激活第一阶段（课题申报）
     *
     * @param initDTO 初始化参数（学期、原因）
     * @return 初始化后的阶段状态
     */
    @PostMapping("/init")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    @Operation(summary = "初始化系统阶段", description = "设置当前学期并激活课题申报阶段，仅系统管理员可操作")
    public Result<PhaseStatusVO> initPhase(@Valid @RequestBody InitPhaseDTO initDTO) {
        log.info("系统阶段初始化请求，学届: {}, 原因: {}", initDTO.getCohort(), initDTO.getReason());
        PhaseStatusVO status = systemPhaseService.initPhase(initDTO);
        return Result.success(status);
    }

    /**
     * 切换系统阶段
     * 仅系统管理员可操作，只能切换到下一阶段，不可回滚
     *
     * @param switchDTO 切换参数（目标阶段代码、切换原因）
     * @return 切换后的阶段状态
     */
    @PostMapping("/switch")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    @Operation(summary = "切换系统阶段", description = "切换到下一阶段，只能前进不可回滚，仅系统管理员可操作")
    public Result<PhaseStatusVO> switchPhase(@Valid @RequestBody SwitchPhaseDTO switchDTO) {
        log.info("系统阶段切换请求，目标阶段: {}", switchDTO.getTargetPhaseCode());
        PhaseStatusVO status = systemPhaseService.switchPhase(switchDTO);
        return Result.success(status);
    }

    /**
     * 查询阶段切换历史记录
     * 仅系统管理员可查看所有切换操作记录
     *
     * @return 切换历史记录列表
     */
    @GetMapping("/records")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    @Operation(summary = "查询切换历史记录", description = "查看所有阶段切换操作的历史记录，仅系统管理员可查看")
    public Result<List<PhaseRecordVO>> getPhaseRecords() {
        List<PhaseRecordVO> records = systemPhaseService.getPhaseRecords();
        return Result.success(records);
    }

    /**
     * 校验指定阶段是否为当前活跃阶段
     *
     * @param phaseCode 阶段代码
     * @return 是否活跃
     */
    @GetMapping("/active/{phaseCode}")
    @Operation(summary = "校验阶段是否活跃", description = "判断指定阶段代码是否为当前活跃阶段")
    public Result<Boolean> isPhaseActive(@PathVariable String phaseCode) {
        boolean active = systemPhaseService.isPhaseActive(phaseCode);
        return Result.success(active);
    }
}

package com.yuwan.completebackend.controller;

import com.yuwan.completebackend.common.Result;
import com.yuwan.completebackend.model.dto.MajorDTO;
import com.yuwan.completebackend.model.dto.MajorDirectionDTO;
import com.yuwan.completebackend.model.vo.*;
import com.yuwan.completebackend.service.IMajorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 专业管理控制器
 * 提供专业方向和专业的CRUD接口
 *
 * @author 系统架构师
 * @version 1.0
 * @since 2026-03-01
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/major")
@Tag(name = "专业管理", description = "专业方向和专业的CRUD接口")
public class MajorController {

    private final IMajorService majorService;

    // ==================== 树型结构查询 ====================

    /**
     * 获取专业树型结构
     * 返回企业的专业方向→专业树型结构
     *
     * @param enterpriseId 企业ID（可选，系统管理员可指定）
     * @param status       状态筛选（可选）
     * @return 树型结构列表
     */
    @GetMapping("/tree")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN', 'ENTERPRISE_LEADER', 'ENTERPRISE_TEACHER')")
    @Operation(summary = "获取专业树型结构", description = "获取企业的专业方向→专业树型结构")
    public Result<List<MajorTreeVO>> getMajorTree(
            @Parameter(description = "企业ID（系统管理员可指定）") @RequestParam(required = false) String enterpriseId,
            @Parameter(description = "状态筛选（0-禁用 1-启用）") @RequestParam(required = false) Integer status) {
        log.info("获取专业树型结构，企业ID: {}, 状态: {}", enterpriseId, status);
        List<MajorTreeVO> result = majorService.getMajorTree(enterpriseId, status);
        return Result.success(result);
    }

    /**
     * 获取级联选择器数据
     * 用于课题创建等场景的级联选择
     *
     * @param enterpriseId 企业ID（可选）
     * @return 级联选择器数据
     */
    @GetMapping("/cascade")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN', 'ENTERPRISE_LEADER', 'ENTERPRISE_TEACHER')")
    @Operation(summary = "获取级联选择器数据", description = "获取专业方向→专业的级联选择数据，用于表单选择")
    public Result<List<MajorCascadeVO>> getCascadeData(
            @Parameter(description = "企业ID（可选）") @RequestParam(required = false) String enterpriseId) {
        log.info("获取级联选择器数据，企业ID: {}", enterpriseId);
        List<MajorCascadeVO> result = majorService.getCascadeData(enterpriseId);
        return Result.success(result);
    }

    // ==================== 专业方向管理 ====================

    /**
     * 添加专业方向
     *
     * @param dto 专业方向表单数据
     * @return 专业方向信息
     */
    @PostMapping("/direction")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN', 'ENTERPRISE_LEADER')")
    @Operation(summary = "添加专业方向", description = "添加新的专业方向")
    public Result<MajorDirectionVO> addDirection(@Valid @RequestBody MajorDirectionDTO dto) {
        log.info("添加专业方向，名称: {}", dto.getDirectionName());
        MajorDirectionVO result = majorService.addDirection(dto);
        return Result.success(result);
    }

    /**
     * 编辑专业方向
     *
     * @param directionId 专业方向ID
     * @param dto         专业方向表单数据
     * @return 专业方向信息
     */
    @PutMapping("/direction/{directionId}")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN', 'ENTERPRISE_LEADER')")
    @Operation(summary = "编辑专业方向", description = "编辑已有的专业方向信息")
    public Result<MajorDirectionVO> updateDirection(
            @Parameter(description = "专业方向ID") @PathVariable String directionId,
            @Valid @RequestBody MajorDirectionDTO dto) {
        log.info("编辑专业方向，方向ID: {}", directionId);
        MajorDirectionVO result = majorService.updateDirection(directionId, dto);
        return Result.success(result);
    }

    /**
     * 删除专业方向
     *
     * @param directionId 专业方向ID
     * @return 操作结果
     */
    @DeleteMapping("/direction/{directionId}")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN', 'ENTERPRISE_LEADER')")
    @Operation(summary = "删除专业方向", description = "删除专业方向，需确保没有子专业")
    public Result<Void> deleteDirection(
            @Parameter(description = "专业方向ID") @PathVariable String directionId) {
        log.info("删除专业方向，方向ID: {}", directionId);
        majorService.deleteDirection(directionId);
        return Result.success();
    }

    /**
     * 获取专业方向详情
     *
     * @param directionId 专业方向ID
     * @return 专业方向信息
     */
    @GetMapping("/direction/{directionId}")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN', 'ENTERPRISE_LEADER', 'ENTERPRISE_TEACHER')")
    @Operation(summary = "获取专业方向详情", description = "获取专业方向详细信息")
    public Result<MajorDirectionVO> getDirectionDetail(
            @Parameter(description = "专业方向ID") @PathVariable String directionId) {
        log.info("获取专业方向详情，方向ID: {}", directionId);
        MajorDirectionVO result = majorService.getDirectionDetail(directionId);
        return Result.success(result);
    }

    /**
     * 获取专业方向列表（下拉选择用）
     *
     * @param enterpriseId 企业ID（可选）
     * @return 专业方向列表
     */
    @GetMapping("/direction/list")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN', 'ENTERPRISE_LEADER', 'ENTERPRISE_TEACHER')")
    @Operation(summary = "获取专业方向列表", description = "获取启用的专业方向列表，用于下拉选择")
    public Result<List<MajorDirectionVO>> getDirectionList(
            @Parameter(description = "企业ID（可选）") @RequestParam(required = false) String enterpriseId) {
        log.info("获取专业方向列表，企业ID: {}", enterpriseId);
        List<MajorDirectionVO> result = majorService.getDirectionList(enterpriseId);
        return Result.success(result);
    }

    /**
     * 切换专业方向状态
     *
     * @param directionId 专业方向ID
     * @param status      目标状态
     * @return 操作结果
     */
    @PutMapping("/direction/{directionId}/status")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN', 'ENTERPRISE_LEADER')")
    @Operation(summary = "切换专业方向状态", description = "启用/禁用专业方向，禁用时级联禁用所有子专业")
    public Result<Void> updateDirectionStatus(
            @Parameter(description = "专业方向ID") @PathVariable String directionId,
            @Parameter(description = "目标状态（0-禁用 1-启用）") @RequestParam Integer status) {
        log.info("切换专业方向状态，方向ID: {}, 状态: {}", directionId, status);
        majorService.updateDirectionStatus(directionId, status);
        return Result.success();
    }

    // ==================== 专业管理 ====================

    /**
     * 添加专业
     *
     * @param dto 专业表单数据
     * @return 专业信息
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN', 'ENTERPRISE_LEADER')")
    @Operation(summary = "添加专业", description = "在指定专业方向下添加新专业")
    public Result<MajorVO> addMajor(@Valid @RequestBody MajorDTO dto) {
        log.info("添加专业，名称: {}, 方向ID: {}", dto.getMajorName(), dto.getDirectionId());
        MajorVO result = majorService.addMajor(dto);
        return Result.success(result);
    }

    /**
     * 编辑专业
     *
     * @param majorId 专业ID
     * @param dto     专业表单数据
     * @return 专业信息
     */
    @PutMapping("/{majorId}")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN', 'ENTERPRISE_LEADER')")
    @Operation(summary = "编辑专业", description = "编辑已有的专业信息")
    public Result<MajorVO> updateMajor(
            @Parameter(description = "专业ID") @PathVariable String majorId,
            @Valid @RequestBody MajorDTO dto) {
        log.info("编辑专业，专业ID: {}", majorId);
        MajorVO result = majorService.updateMajor(majorId, dto);
        return Result.success(result);
    }

    /**
     * 删除专业
     *
     * @param majorId 专业ID
     * @return 操作结果
     */
    @DeleteMapping("/{majorId}")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN', 'ENTERPRISE_LEADER')")
    @Operation(summary = "删除专业", description = "删除专业，需确保没有关联课题")
    public Result<Void> deleteMajor(
            @Parameter(description = "专业ID") @PathVariable String majorId) {
        log.info("删除专业，专业ID: {}", majorId);
        majorService.deleteMajor(majorId);
        return Result.success();
    }

    /**
     * 获取专业详情
     *
     * @param majorId 专业ID
     * @return 专业信息
     */
    @GetMapping("/{majorId}")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN', 'ENTERPRISE_LEADER', 'ENTERPRISE_TEACHER')")
    @Operation(summary = "获取专业详情", description = "获取专业详细信息")
    public Result<MajorVO> getMajorDetail(
            @Parameter(description = "专业ID") @PathVariable String majorId) {
        log.info("获取专业详情，专业ID: {}", majorId);
        MajorVO result = majorService.getMajorDetail(majorId);
        return Result.success(result);
    }

    /**
     * 切换专业状态
     *
     * @param majorId 专业ID
     * @param status  目标状态
     * @return 操作结果
     */
    @PutMapping("/{majorId}/status")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN', 'ENTERPRISE_LEADER')")
    @Operation(summary = "切换专业状态", description = "启用/禁用专业")
    public Result<Void> updateMajorStatus(
            @Parameter(description = "专业ID") @PathVariable String majorId,
            @Parameter(description = "目标状态（0-禁用 1-启用）") @RequestParam Integer status) {
        log.info("切换专业状态，专业ID: {}, 状态: {}", majorId, status);
        majorService.updateMajorStatus(majorId, status);
        return Result.success();
    }
}

package com.yuwan.completebackend.controller;

import com.yuwan.completebackend.common.PageResult;
import com.yuwan.completebackend.common.Result;
import com.yuwan.completebackend.model.dto.CreateEnterpriseDTO;
import com.yuwan.completebackend.model.dto.UpdateEnterpriseDTO;
import com.yuwan.completebackend.model.vo.EnterpriseOverviewVO;
import com.yuwan.completebackend.model.vo.EnterpriseQueryVO;
import com.yuwan.completebackend.model.vo.EnterpriseVO;
import com.yuwan.completebackend.service.IEnterpriseService;
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
 * 企业管理控制器
 * 提供企业信息的CRUD接口
 *
 * @author 系统架构师
 * @version 1.0
 * @since 2026-02-21
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/enterprise")
@Tag(name = "企业管理", description = "企业信息CRUD接口")
public class EnterpriseController {

    private final IEnterpriseService enterpriseService;

    /**
     * 创建企业
     *
     * @param createDTO 创建企业请求参数
     * @return 企业信息
     */
    @PostMapping
    @PreAuthorize("hasRole('SYSTEM_ADMIN') or hasRole('ENTERPRISE_LEADER')")
    @Operation(summary = "创建企业", description = "管理员或企业负责人创建新企业信息")
    public Result<EnterpriseVO> createEnterprise(@Valid @RequestBody CreateEnterpriseDTO createDTO) {
        log.info("创建企业请求，企业名称: {}", createDTO.getEnterpriseName());
        EnterpriseVO result = enterpriseService.createEnterprise(createDTO);
        return Result.success(result);
    }

    /**
     * 更新企业
     *
     * @param enterpriseId 企业ID
     * @param updateDTO 更新企业请求参数
     * @return 企业信息
     */
    @PutMapping("/{enterpriseId}")
    @PreAuthorize("hasRole('SYSTEM_ADMIN') or hasRole('ENTERPRISE_LEADER')")
    @Operation(summary = "更新企业", description = "管理员或企业负责人更新企业信息")
    public Result<EnterpriseVO> updateEnterprise(
            @Parameter(description = "企业ID") @PathVariable String enterpriseId,
            @Valid @RequestBody UpdateEnterpriseDTO updateDTO) {
        log.info("更新企业请求，企业ID: {}", enterpriseId);
        EnterpriseVO result = enterpriseService.updateEnterprise(enterpriseId, updateDTO);
        return Result.success(result);
    }

    /**
     * 获取企业详情
     *
     * @param enterpriseId 企业ID
     * @return 企业信息
     */
    @GetMapping("/{enterpriseId}")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    @Operation(summary = "获取企业详情", description = "根据ID获取企业详细信息")
    public Result<EnterpriseVO> getEnterpriseDetail(
            @Parameter(description = "企业ID") @PathVariable String enterpriseId) {
        log.info("获取企业详情，企业ID: {}", enterpriseId);
        EnterpriseVO result = enterpriseService.getEnterpriseDetail(enterpriseId);
        return Result.success(result);
    }

    /**
     * 分页查询企业列表
     *
     * @param queryVO 查询参数
     * @return 分页企业列表
     */
    @GetMapping("/list")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    @Operation(summary = "分页查询企业列表", description = "根据条件分页查询企业列表")
    public Result<PageResult<EnterpriseVO>> getEnterpriseList(EnterpriseQueryVO queryVO) {
        log.info("分页查询企业列表，页码: {}, 页大小: {}", queryVO.getPageNum(), queryVO.getPageSize());
        PageResult<EnterpriseVO> result = enterpriseService.getEnterpriseList(queryVO);
        return Result.success(result);
    }

    /**
     * 获取企业概览（包含统计数据）
     *
     * @param queryVO 查询参数
     * @return 企业概览分页数据
     */
    @GetMapping("/overview")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    @Operation(summary = "获取企业概览", description = "查询企业列表及统计数据（方向数、专业数、教师数、学生数）")
    public Result<PageResult<EnterpriseOverviewVO>> getEnterpriseOverview(EnterpriseQueryVO queryVO) {
        log.info("获取企业概览，页码: {}, 页大小: {}", queryVO.getPageNum(), queryVO.getPageSize());
        PageResult<EnterpriseOverviewVO> result = enterpriseService.getEnterpriseOverview(queryVO);
        return Result.success(result);
    }

    /**
     * 获取全部启用企业（下拉选择用）
     *
     * @return 企业列表
     */
    @GetMapping("/all")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN', 'ENTERPRISE_TEACHER', 'ENTERPRISE_LEADER')")
    @Operation(summary = "获取全部启用企业", description = "获取全部启用状态的企业，用于下拉选择")
    public Result<List<EnterpriseVO>> getAllEnterprises() {
        log.info("获取全部启用企业");
        List<EnterpriseVO> result = enterpriseService.getAllEnterprises();
        return Result.success(result);
    }

    /**
     * 删除企业
     *
     * @param enterpriseId 企业ID
     * @return 操作结果
     */
    @DeleteMapping("/{enterpriseId}")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    @Operation(summary = "删除企业", description = "管理员删除企业信息")
    public Result<Void> deleteEnterprise(
            @Parameter(description = "企业ID") @PathVariable String enterpriseId) {
        log.info("删除企业请求，企业ID: {}", enterpriseId);
        enterpriseService.deleteEnterprise(enterpriseId);
        return Result.success();
    }

    /**
     * 更新企业状态
     *
     * @param enterpriseId 企业ID
     * @param status 状态(0-禁用, 1-启用)
     * @return 操作结果
     */
    @PutMapping("/{enterpriseId}/status")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    @Operation(summary = "更新企业状态", description = "管理员更新企业启用/禁用状态")
    public Result<Void> updateEnterpriseStatus(
            @Parameter(description = "企业ID") @PathVariable String enterpriseId,
            @Parameter(description = "状态(0-禁用, 1-启用)") @RequestParam Integer status) {
        log.info("更新企业状态请求，企业ID: {}, 状态: {}", enterpriseId, status);
        enterpriseService.updateEnterpriseStatus(enterpriseId, status);
        return Result.success();
    }
}

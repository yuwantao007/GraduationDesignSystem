package com.yuwan.completebackend.controller;



import com.yuwan.completebackend.common.PageResult;
import com.yuwan.completebackend.common.Result;
import com.yuwan.completebackend.model.dto.CreateSchoolDTO;
import com.yuwan.completebackend.model.dto.UpdateSchoolDTO;
import com.yuwan.completebackend.model.vo.SchoolQueryVO;
import com.yuwan.completebackend.model.vo.SchoolVO;
import com.yuwan.completebackend.service.ISchoolService;
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
 * 学校管理控制器
 * 提供学校信息的CRUD接口
 * 仅管理员可访问
 *
 * @author 系统架构师
 * @version 1.0
 * @since 2026-02-22
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/school")
@Tag(name = "学校管理", description = "学校信息CRUD接口")
public class SchoolController {

    private final ISchoolService schoolService;

    /**
     * 创建学校
     *
     * @param createDTO 创建学校请求参数
     * @return 学校信息
     */
    @PostMapping
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    @Operation(summary = "创建学校", description = "管理员创建新学校信息")
    public Result<SchoolVO> createSchool(@Valid @RequestBody CreateSchoolDTO createDTO) {
        log.info("创建学校请求，学校名称: {}", createDTO.getSchoolName());
        SchoolVO result = schoolService.createSchool(createDTO);
        return Result.success(result);
    }

    /**
     * 更新学校
     *
     * @param schoolId 学校ID
     * @param updateDTO 更新学校请求参数
     * @return 学校信息
     */
    @PutMapping("/{schoolId}")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    @Operation(summary = "更新学校", description = "管理员更新学校信息")
    public Result<SchoolVO> updateSchool(
            @Parameter(description = "学校ID") @PathVariable String schoolId,
            @Valid @RequestBody UpdateSchoolDTO updateDTO) {
        log.info("更新学校请求，学校ID: {}", schoolId);
        SchoolVO result = schoolService.updateSchool(schoolId, updateDTO);
        return Result.success(result);
    }

    /**
     * 获取学校详情
     *
     * @param schoolId 学校ID
     * @return 学校信息
     */
    @GetMapping("/{schoolId}")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    @Operation(summary = "获取学校详情", description = "根据ID获取学校详细信息")
    public Result<SchoolVO> getSchoolDetail(
            @Parameter(description = "学校ID") @PathVariable String schoolId) {
        log.info("获取学校详情，学校ID: {}", schoolId);
        SchoolVO result = schoolService.getSchoolDetail(schoolId);
        return Result.success(result);
    }

    /**
     * 分页查询学校列表
     *
     * @param queryVO 查询参数
     * @return 分页学校列表
     */
    @GetMapping("/list")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    @Operation(summary = "分页查询学校列表", description = "根据条件分页查询学校列表")
    public Result<PageResult<SchoolVO>> getSchoolList(SchoolQueryVO queryVO) {
        log.info("分页查询学校列表，页码: {}, 页大小: {}", queryVO.getPageNum(), queryVO.getPageSize());
        PageResult<SchoolVO> result = schoolService.getSchoolList(queryVO);
        return Result.success(result);
    }

    /**
     * 获取全部启用学校（下拉选择用）
     *
     * @return 学校列表
     */
    @GetMapping("/all")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    @Operation(summary = "获取全部启用学校", description = "获取全部启用状态的学校，用于下拉选择")
    public Result<List<SchoolVO>> getAllSchools() {
        log.info("获取全部启用学校");
        List<SchoolVO> result = schoolService.getAllSchools();
        return Result.success(result);
    }

    /**
     * 删除学校
     *
     * @param schoolId 学校ID
     * @return 操作结果
     */
    @DeleteMapping("/{schoolId}")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    @Operation(summary = "删除学校", description = "管理员删除学校信息")
    public Result<Void> deleteSchool(
            @Parameter(description = "学校ID") @PathVariable String schoolId) {
        log.info("删除学校请求，学校ID: {}", schoolId);
        schoolService.deleteSchool(schoolId);
        return Result.success();
    }

    /**
     * 更新学校状态
     *
     * @param schoolId 学校ID
     * @param status 状态(0-禁用, 1-启用)
     * @return 操作结果
     */
    @PutMapping("/{schoolId}/status")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    @Operation(summary = "更新学校状态", description = "管理员更新学校启用/禁用状态")
    public Result<Void> updateSchoolStatus(
            @Parameter(description = "学校ID") @PathVariable String schoolId,
            @Parameter(description = "状态(0-禁用, 1-启用)") @RequestParam Integer status) {
        log.info("更新学校状态请求，学校ID: {}, 状态: {}", schoolId, status);
        schoolService.updateSchoolStatus(schoolId, status);
        return Result.success();
    }
}

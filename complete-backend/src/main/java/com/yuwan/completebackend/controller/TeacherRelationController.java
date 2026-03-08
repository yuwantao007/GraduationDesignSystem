package com.yuwan.completebackend.controller;

import com.yuwan.completebackend.common.Result;
import com.yuwan.completebackend.model.dto.TeacherRelationDTO;
import com.yuwan.completebackend.model.dto.UnivTeacherMajorDTO;
import com.yuwan.completebackend.model.vo.TeacherCoverageVO;
import com.yuwan.completebackend.model.vo.TeacherRelationVO;
import com.yuwan.completebackend.model.vo.UnivTeacherMajorVO;
import com.yuwan.completebackend.service.ITeacherRelationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 教师配对管理控制器
 * <p>
 * 提供高校教师与企业教师/专业方向的双层配对管理接口：
 * <ul>
 *   <li>第一层（粗粒度）：方向级分配 —— 高校教师 → 专业方向</li>
 *   <li>第二层（细粒度）：精确配对 —— 高校教师 ↔ 企业教师</li>
 *   <li>覆盖检查：统计企业教师的配对覆盖情况</li>
 * </ul>
 * </p>
 *
 * @author 系统架构师
 * @version 1.0
 * @since 2026-03-08
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/teacher-relation")
@Tag(name = "教师配对管理", description = "高校教师与企业教师/专业方向的双层配对管理接口")
public class TeacherRelationController {

    private final ITeacherRelationService teacherRelationService;

    // ==================== 第一层：方向级分配 ====================

    /**
     * 查询方向级分配列表
     *
     * @param enterpriseId 企业ID（可选）
     * @param cohort       届别（可选）
     * @return 分配列表
     */
    @GetMapping("/major-assignment/list")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN', 'ENTERPRISE_LEADER', 'UNIVERSITY_TEACHER')")
    @Operation(summary = "查询方向级分配列表", description = "查询高校教师与专业方向的分配记录")
    public Result<List<UnivTeacherMajorVO>> listMajorAssignments(
            @Parameter(description = "企业ID") @RequestParam(required = false) String enterpriseId,
            @Parameter(description = "届别") @RequestParam(required = false) String cohort) {
        log.info("查询方向级分配列表，企业ID={}, 届别={}", enterpriseId, cohort);
        List<UnivTeacherMajorVO> result = teacherRelationService.listMajorAssignments(enterpriseId, cohort);
        return Result.success(result);
    }

    /**
     * 新增方向级分配
     *
     * @param dto 分配参数
     * @return 分配结果
     */
    @PostMapping("/major-assignment")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    @Operation(summary = "新增方向级分配", description = "为高校教师分配对接的专业方向")
    public Result<UnivTeacherMajorVO> addMajorAssignment(@Valid @RequestBody UnivTeacherMajorDTO dto) {
        log.info("新增方向级分配，高校教师={}, 方向={}", dto.getUnivTeacherId(), dto.getDirectionId());
        UnivTeacherMajorVO result = teacherRelationService.addMajorAssignment(dto);
        return Result.success(result);
    }

    /**
     * 编辑方向级分配
     *
     * @param id  分配ID
     * @param dto 修改参数
     * @return 修改结果
     */
    @PutMapping("/major-assignment/{id}")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    @Operation(summary = "编辑方向级分配", description = "修改高校教师的方向级分配")
    public Result<UnivTeacherMajorVO> updateMajorAssignment(
            @Parameter(description = "分配ID") @PathVariable String id,
            @Valid @RequestBody UnivTeacherMajorDTO dto) {
        log.info("编辑方向级分配，id={}", id);
        UnivTeacherMajorVO result = teacherRelationService.updateMajorAssignment(id, dto);
        return Result.success(result);
    }

    /**
     * 删除方向级分配
     *
     * @param id 分配ID
     * @return 操作结果
     */
    @DeleteMapping("/major-assignment/{id}")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    @Operation(summary = "删除方向级分配", description = "删除高校教师的方向级分配")
    public Result<Void> deleteMajorAssignment(
            @Parameter(description = "分配ID") @PathVariable String id) {
        log.info("删除方向级分配，id={}", id);
        teacherRelationService.deleteMajorAssignment(id);
        return Result.success();
    }

    // ==================== 第二层：精确配对 ====================

    /**
     * 查询精确配对列表
     *
     * @param enterpriseId 企业ID（可选）
     * @param cohort       届别（可选）
     * @return 配对列表
     */
    @GetMapping("/teacher-pair/list")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN', 'ENTERPRISE_LEADER', 'UNIVERSITY_TEACHER')")
    @Operation(summary = "查询精确配对列表", description = "查询高校教师与企业教师的精确配对记录")
    public Result<List<TeacherRelationVO>> listTeacherPairs(
            @Parameter(description = "企业ID") @RequestParam(required = false) String enterpriseId,
            @Parameter(description = "届别") @RequestParam(required = false) String cohort) {
        log.info("查询精确配对列表，企业ID={}, 届别={}", enterpriseId, cohort);
        List<TeacherRelationVO> result = teacherRelationService.listTeacherPairs(enterpriseId, cohort);
        return Result.success(result);
    }

    /**
     * 新增精确配对
     *
     * @param dto 配对参数
     * @return 配对结果
     */
    @PostMapping("/teacher-pair")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    @Operation(summary = "新增精确配对", description = "创建高校教师与企业教师的精确配对")
    public Result<TeacherRelationVO> addTeacherPair(@Valid @RequestBody TeacherRelationDTO dto) {
        log.info("新增精确配对，高校教师={}, 企业教师={}", dto.getUnivTeacherId(), dto.getEnterpriseTeacherId());
        TeacherRelationVO result = teacherRelationService.addTeacherPair(dto);
        return Result.success(result);
    }

    /**
     * 编辑精确配对
     *
     * @param relationId 配对ID
     * @param dto        修改参数
     * @return 修改结果
     */
    @PutMapping("/teacher-pair/{relationId}")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    @Operation(summary = "编辑精确配对", description = "修改高校教师与企业教师的精确配对")
    public Result<TeacherRelationVO> updateTeacherPair(
            @Parameter(description = "配对ID") @PathVariable String relationId,
            @Valid @RequestBody TeacherRelationDTO dto) {
        log.info("编辑精确配对，relationId={}", relationId);
        TeacherRelationVO result = teacherRelationService.updateTeacherPair(relationId, dto);
        return Result.success(result);
    }

    /**
     * 删除精确配对
     *
     * @param relationId 配对ID
     * @return 操作结果
     */
    @DeleteMapping("/teacher-pair/{relationId}")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    @Operation(summary = "删除精确配对", description = "删除高校教师与企业教师的精确配对")
    public Result<Void> deleteTeacherPair(
            @Parameter(description = "配对ID") @PathVariable String relationId) {
        log.info("删除精确配对，relationId={}", relationId);
        teacherRelationService.deleteTeacherPair(relationId);
        return Result.success();
    }

    // ==================== 覆盖检查 ====================

    /**
     * 获取配对覆盖情况列表
     *
     * @param enterpriseId 企业ID（可选）
     * @param cohort       届别
     * @return 覆盖情况列表
     */
    @GetMapping("/coverage/list")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN', 'ENTERPRISE_LEADER', 'UNIVERSITY_TEACHER')")
    @Operation(summary = "获取配对覆盖情况", description = "查询企业教师的高校教师配对覆盖情况")
    public Result<List<TeacherCoverageVO>> getCoverageList(
            @Parameter(description = "企业ID") @RequestParam(required = false) String enterpriseId,
            @Parameter(description = "届别") @RequestParam(required = false) String cohort) {
        log.info("获取配对覆盖情况，企业ID={}, 届别={}", enterpriseId, cohort);
        List<TeacherCoverageVO> result = teacherRelationService.getCoverageList(enterpriseId, cohort);
        return Result.success(result);
    }

    /**
     * 获取配对覆盖率统计
     *
     * @param cohort 届别
     * @return 统计数据
     */
    @GetMapping("/coverage/stats")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN', 'ENTERPRISE_LEADER', 'UNIVERSITY_TEACHER')")
    @Operation(summary = "获取配对覆盖率统计", description = "统计指定届别的教师配对覆盖率")
    public Result<Map<String, Integer>> getCoverageStats(
            @Parameter(description = "届别") @RequestParam(required = false) String cohort) {
        log.info("获取配对覆盖率统计，届别={}", cohort);
        Map<String, Integer> result = teacherRelationService.getCoverageStats(cohort);
        return Result.success(result);
    }

    /**
     * 查找企业教师对应的高校教师（双层查找）
     *
     * @param enterpriseTeacherId 企业教师ID
     * @param directionId         专业方向ID
     * @param cohort              届别
     * @return 高校教师 user_id
     */
    @GetMapping("/find-univ-teacher")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN', 'ENTERPRISE_LEADER', 'ENTERPRISE_TEACHER', 'UNIVERSITY_TEACHER')")
    @Operation(summary = "查找对应高校教师", description = "根据企业教师ID和方向查找对应的高校教师（优先精确配对，兜底方向级分配）")
    public Result<String> findUniversityTeacher(
            @Parameter(description = "企业教师ID") @RequestParam String enterpriseTeacherId,
            @Parameter(description = "专业方向ID") @RequestParam(required = false) String directionId,
            @Parameter(description = "届别") @RequestParam(required = false) String cohort) {
        log.info("查找高校教师，企业教师={}, 方向={}, 届别={}", enterpriseTeacherId, directionId, cohort);
        String univTeacherId = teacherRelationService.findUniversityTeacher(enterpriseTeacherId, directionId, cohort);
        return Result.success(univTeacherId);
    }
}

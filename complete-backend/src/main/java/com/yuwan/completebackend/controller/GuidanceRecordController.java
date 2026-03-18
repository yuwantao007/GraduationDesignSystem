package com.yuwan.completebackend.controller;

import com.yuwan.completebackend.common.PageResult;
import com.yuwan.completebackend.common.Result;
import com.yuwan.completebackend.model.dto.CreateGuidanceDTO;
import com.yuwan.completebackend.model.vo.GuidanceListVO;
import com.yuwan.completebackend.model.vo.GuidanceQueryVO;
import com.yuwan.completebackend.model.vo.GuidanceRecordVO;
import com.yuwan.completebackend.model.vo.GuidanceStudentVO;
import com.yuwan.completebackend.service.IGuidanceRecordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * 指导记录控制器
 * 提供企业教师项目指导、高校教师论文指导相关接口
 *
 * @author 系统架构师
 * @version 1.0
 * @since 2026-03-16
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/guidance/record")
@Tag(name = "指导记录", description = "指导记录管理接口（教师指导 / 学生查看 / 负责人概览）")
public class GuidanceRecordController {

    private final IGuidanceRecordService guidanceRecordService;

    // ==================== 教师操作 ====================

    /**
     * 新增指导记录（企业教师/高校教师）
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ENTERPRISE_TEACHER', 'UNIVERSITY_TEACHER')")
    @Operation(summary = "新增指导记录", description = "企业教师添加项目指导记录，高校教师添加论文指导记录")
    public Result<GuidanceRecordVO> createGuidanceRecord(@Valid @RequestBody CreateGuidanceDTO dto) {
        log.info("新增指导记录，学生: {}, 课题: {}", dto.getStudentId(), dto.getTopicId());
        return Result.success(guidanceRecordService.createGuidanceRecord(dto));
    }

    /**
     * 删除指导记录（本人可删）
     */
    @DeleteMapping("/{recordId}")
    @PreAuthorize("hasAnyRole('ENTERPRISE_TEACHER', 'UNIVERSITY_TEACHER')")
    @Operation(summary = "删除指导记录", description = "只能删除自己创建的指导记录")
    public Result<Void> deleteGuidanceRecord(
            @Parameter(description = "记录ID") @PathVariable String recordId) {
        log.info("删除指导记录: {}", recordId);
        guidanceRecordService.deleteGuidanceRecord(recordId);
        return Result.success();
    }

    /**
     * 我的学生列表（教师视角，含最新指导时间）
     */
    @GetMapping("/my-students")
    @PreAuthorize("hasAnyRole('ENTERPRISE_TEACHER', 'UNIVERSITY_TEACHER')")
    @Operation(summary = "我的学生列表", description = "教师查看自己指导的学生列表，含最新指导时间和统计信息")
    public Result<List<GuidanceStudentVO>> getMyStudents() {
        return Result.success(guidanceRecordService.getMyStudents());
    }

    /**
     * 查看某学生的全部指导记录
     */
    @GetMapping("/student/{studentId}")
    @PreAuthorize("hasAnyRole('ENTERPRISE_TEACHER', 'UNIVERSITY_TEACHER', 'ENTERPRISE_LEADER')")
    @Operation(summary = "学生指导记录", description = "查看某学生的全部指导记录")
    public Result<List<GuidanceListVO>> getStudentGuidanceRecords(
            @Parameter(description = "学生ID") @PathVariable String studentId) {
        return Result.success(guidanceRecordService.getStudentGuidanceRecords(studentId));
    }

    // ==================== 学生操作 ====================

    /**
     * 我的被指导记录（学生视角）
     */
    @GetMapping("/my")
    @PreAuthorize("hasRole('STUDENT')")
    @Operation(summary = "我的指导记录", description = "学生查看自己的被指导记录列表")
    public Result<List<GuidanceListVO>> getMyGuidanceRecords() {
        return Result.success(guidanceRecordService.getMyGuidanceRecords());
    }

    // ==================== 企业负责人操作 ====================

    /**
     * 指导记录总览（企业负责人，含筛选分页）
     */
    @GetMapping("/leader/list")
    @PreAuthorize("hasRole('ENTERPRISE_LEADER')")
    @Operation(summary = "指导记录总览", description = "企业负责人查看企业内全部指导记录，支持筛选和分页")
    public Result<PageResult<GuidanceListVO>> getLeaderGuidanceOverview(GuidanceQueryVO queryVO) {
        return Result.success(guidanceRecordService.getLeaderGuidanceOverview(queryVO));
    }

    /**
     * 导出指导记录 Excel（企业负责人）
     */
    @GetMapping("/leader/export")
    @PreAuthorize("hasRole('ENTERPRISE_LEADER')")
    @Operation(summary = "导出指导记录", description = "企业负责人导出指导记录Excel文件")
    public void exportGuidanceRecords(GuidanceQueryVO queryVO, HttpServletResponse response) {
        try {
            byte[] data = guidanceRecordService.exportGuidanceRecords(queryVO);
            
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            String fileName = URLEncoder.encode("指导记录.xlsx", StandardCharsets.UTF_8);
            response.setHeader("Content-Disposition", "attachment; filename=" + fileName);
            response.getOutputStream().write(data);
            response.getOutputStream().flush();
        } catch (Exception e) {
            log.error("导出指导记录失败", e);
            throw new RuntimeException("导出失败");
        }
    }

    // ==================== 通用查询 ====================

    /**
     * 指导记录详情
     */
    @GetMapping("/{recordId}")
    @PreAuthorize("hasAnyRole('ENTERPRISE_TEACHER', 'UNIVERSITY_TEACHER', 'ENTERPRISE_LEADER', 'STUDENT')")
    @Operation(summary = "指导记录详情", description = "查看指导记录详细信息")
    public Result<GuidanceRecordVO> getGuidanceRecordDetail(
            @Parameter(description = "记录ID") @PathVariable String recordId) {
        return Result.success(guidanceRecordService.getGuidanceRecordDetail(recordId));
    }
}

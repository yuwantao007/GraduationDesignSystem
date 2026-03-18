package com.yuwan.completebackend.controller;

import com.yuwan.completebackend.common.PageResult;
import com.yuwan.completebackend.common.Result;
import com.yuwan.completebackend.model.dto.midterm.CreateMidtermCheckDTO;
import com.yuwan.completebackend.model.dto.midterm.MidtermCheckQueryDTO;
import com.yuwan.completebackend.model.dto.midterm.ReviewMidtermCheckDTO;
import com.yuwan.completebackend.model.vo.MidtermCheckListVO;
import com.yuwan.completebackend.model.vo.MidtermCheckVO;
import com.yuwan.completebackend.service.IMidtermCheckService;
import com.yuwan.completebackend.security.SecurityUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 中期检查控制器
 *
 * @author 系统架构师
 * @version 1.0
 * @since 2026-03-17
 */
@Slf4j
@RestController
@RequestMapping("/api/midterm")
@RequiredArgsConstructor
@Tag(name = "中期检查管理", description = "中期检查表相关接口")
public class MidtermController {

    private final IMidtermCheckService midtermCheckService;

    // ==================== 企业教师接口 ====================

    @Operation(summary = "企业教师-创建/编辑中期检查表")
    @PostMapping("/enterprise/save")
    @PreAuthorize("hasAuthority('midterm:enterprise:edit')")
    public Result<String> saveCheck(@Valid @RequestBody CreateMidtermCheckDTO dto) {
        String teacherId = SecurityUtil.getCurrentUserId();
        String checkId = midtermCheckService.createOrUpdateCheck(dto, teacherId);
        return Result.success("保存成功", checkId);
    }

    @Operation(summary = "企业教师-提交中期检查表")
    @PostMapping("/enterprise/submit/{checkId}")
    @PreAuthorize("hasAuthority('midterm:enterprise:submit')")
    public Result<Boolean> submitCheck(
            @Parameter(description = "检查表ID") @PathVariable String checkId) {
        String teacherId = SecurityUtil.getCurrentUserId();
        Boolean success = midtermCheckService.submitCheck(checkId, teacherId);
        return Result.success("提交成功", success);
    }

    @Operation(summary = "企业教师-查询负责的中期检查列表")
    @GetMapping("/enterprise/list")
    @PreAuthorize("hasAuthority('midterm:enterprise:list')")
    public Result<PageResult<MidtermCheckListVO>> getEnterpriseList(MidtermCheckQueryDTO queryDTO) {
        String teacherId = SecurityUtil.getCurrentUserId();
        PageResult<MidtermCheckListVO> result = midtermCheckService.getEnterpriseTeacherList(teacherId, queryDTO);
        return Result.success("查询成功", result);
    }

    @Operation(summary = "企业教师-获取中期检查表详情")
    @GetMapping("/enterprise/detail/{checkId}")
    @PreAuthorize("hasAuthority('midterm:enterprise:detail')")
    public Result<MidtermCheckVO> getEnterpriseDetail(
            @Parameter(description = "检查表ID") @PathVariable String checkId) {
        MidtermCheckVO vo = midtermCheckService.getCheckDetail(checkId);
        return Result.success("查询成功", vo);
    }

    // ==================== 高校教师接口 ====================

    @Operation(summary = "高校教师-审查中期检查表")
    @PostMapping("/univ/review")
    @PreAuthorize("hasAuthority('midterm:univ:review')")
    public Result<Boolean> reviewCheck(@Valid @RequestBody ReviewMidtermCheckDTO dto) {
        String reviewerId = SecurityUtil.getCurrentUserId();
        Boolean success = midtermCheckService.reviewCheck(dto, reviewerId);
        return Result.success("审查成功", success);
    }

    @Operation(summary = "高校教师-查询待审查的中期检查列表")
    @GetMapping("/univ/list")
    @PreAuthorize("hasAuthority('midterm:univ:list')")
    public Result<PageResult<MidtermCheckListVO>> getUnivList(MidtermCheckQueryDTO queryDTO) {
        String teacherId = SecurityUtil.getCurrentUserId();
        PageResult<MidtermCheckListVO> result = midtermCheckService.getUnivTeacherList(teacherId, queryDTO);
        return Result.success("查询成功", result);
    }

    @Operation(summary = "高校教师-获取中期检查表详情")
    @GetMapping("/univ/detail/{checkId}")
    @PreAuthorize("hasAuthority('midterm:univ:detail')")
    public Result<MidtermCheckVO> getUnivDetail(
            @Parameter(description = "检查表ID") @PathVariable String checkId) {
        MidtermCheckVO vo = midtermCheckService.getCheckDetail(checkId);
        return Result.success("查询成功", vo);
    }

    // ==================== 学生接口 ====================

    @Operation(summary = "学生-查看自己的中期检查表")
    @GetMapping("/student/my")
    @PreAuthorize("hasAuthority('midterm:student:view')")
    public Result<MidtermCheckVO> getStudentCheck() {
        String studentId = SecurityUtil.getCurrentUserId();
        MidtermCheckVO vo = midtermCheckService.getByStudentId(studentId);
        return Result.success("查询成功", vo);
    }

    // ==================== 管理员接口 ====================

    @Operation(summary = "管理员-查询所有中期检查列表")
    @GetMapping("/admin/list")
    @PreAuthorize("hasAuthority('midterm:admin:list')")
    public Result<PageResult<MidtermCheckListVO>> getAdminList(MidtermCheckQueryDTO queryDTO) {
        PageResult<MidtermCheckListVO> result = midtermCheckService.getAdminList(queryDTO);
        return Result.success("查询成功", result);
    }

    @Operation(summary = "管理员-获取中期检查表详情")
    @GetMapping("/admin/detail/{checkId}")
    @PreAuthorize("hasAuthority('midterm:admin:detail')")
    public Result<MidtermCheckVO> getAdminDetail(
            @Parameter(description = "检查表ID") @PathVariable String checkId) {
        MidtermCheckVO vo = midtermCheckService.getCheckDetail(checkId);
        return Result.success("查询成功", vo);
    }
}

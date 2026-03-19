package com.yuwan.completebackend.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.yuwan.completebackend.common.Result;
import com.yuwan.completebackend.model.dto.defense.*;
import com.yuwan.completebackend.model.vo.defense.DefenseArrangementVO;
import com.yuwan.completebackend.model.vo.defense.OpeningReportVO;
import com.yuwan.completebackend.model.vo.defense.OpeningTaskBookVO;
import com.yuwan.completebackend.service.IDefenseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 开题答辩管理Controller
 *
 * @author 系统架构师
 * @version 1.0
 * @since 2026-03-17
 */
@Tag(name = "开题答辩管理", description = "答辩安排、任务书、开题报告管理接口")
@RestController
@RequestMapping("/defense")
@RequiredArgsConstructor
public class DefenseController {

    private final IDefenseService defenseService;

    // ==================== 答辩安排管理（企业负责人） ====================

    @Operation(summary = "创建答辩安排", description = "企业负责人创建答辩安排")
    @PostMapping("/arrangement")
    @PreAuthorize("hasAuthority('defense:arrangement:create')")
    public Result<String> createArrangement(@Valid @RequestBody CreateArrangementDTO dto) {
        String arrangementId = defenseService.createArrangement(dto);
        return Result.success("创建成功", arrangementId);
    }

    @Operation(summary = "更新答辩安排", description = "企业负责人更新答辩安排")
    @PutMapping("/arrangement")
    @PreAuthorize("hasAuthority('defense:arrangement:update')")
    public Result<Boolean> updateArrangement(@Valid @RequestBody UpdateArrangementDTO dto) {
        Boolean success = defenseService.updateArrangement(dto);
        return Result.success("更新成功", success);
    }

    @Operation(summary = "删除答辩安排", description = "企业负责人删除答辩安排")
    @DeleteMapping("/arrangement/{arrangementId}")
    @PreAuthorize("hasAuthority('defense:arrangement:delete')")
    public Result<Boolean> deleteArrangement(
            @Parameter(description = "安排ID") @PathVariable String arrangementId) {
        Boolean success = defenseService.deleteArrangement(arrangementId);
        return Result.success("删除成功", success);
    }

    @Operation(summary = "分页查询答辩安排列表", description = "查询当前企业的答辩安排列表")
    @GetMapping("/arrangement/page")
    @PreAuthorize("hasAuthority('defense:arrangement:list')")
    public Result<IPage<DefenseArrangementVO>> pageArrangements(ArrangementQueryDTO queryDTO) {
        IPage<DefenseArrangementVO> page = defenseService.pageArrangements(queryDTO);
        return Result.success(page);
    }

    @Operation(summary = "获取答辩安排详情", description = "根据ID获取答辩安排详情")
    @GetMapping("/arrangement/{arrangementId}")
    @PreAuthorize("hasAuthority('defense:arrangement:detail')")
    public Result<DefenseArrangementVO> getArrangementDetail(
            @Parameter(description = "安排ID") @PathVariable String arrangementId) {
        DefenseArrangementVO vo = defenseService.getArrangementDetail(arrangementId);
        return Result.success(vo);
    }

    // ==================== 开题任务书管理（企业教师） ====================

    @Operation(summary = "保存任务书", description = "企业教师保存/更新学生任务书")
    @PostMapping("/taskbook")
    @PreAuthorize("hasAuthority('defense:taskbook:save')")
    public Result<String> saveTaskBook(@Valid @RequestBody SaveTaskBookDTO dto) {
        String taskBookId = defenseService.saveTaskBook(dto);
        return Result.success("保存成功", taskBookId);
    }

    @Operation(summary = "获取学生任务书", description = "根据学生ID获取任务书详情")
    @GetMapping("/taskbook/student/{studentId}")
    @PreAuthorize("hasAuthority('defense:taskbook:detail')")
    public Result<OpeningTaskBookVO> getTaskBookByStudent(
            @Parameter(description = "学生ID") @PathVariable String studentId) {
        OpeningTaskBookVO vo = defenseService.getTaskBookByStudent(studentId);
        return Result.success(vo);
    }

    @Operation(summary = "获取任务书详情", description = "根据任务书ID获取详情")
    @GetMapping("/taskbook/{taskBookId}")
    @PreAuthorize("hasAuthority('defense:taskbook:detail')")
    public Result<OpeningTaskBookVO> getTaskBookDetail(
            @Parameter(description = "任务书ID") @PathVariable String taskBookId) {
        OpeningTaskBookVO vo = defenseService.getTaskBookDetail(taskBookId);
        return Result.success(vo);
    }

    // ==================== 开题报告管理（学生提交+企业教师审查） ====================

    @Operation(summary = "学生提交开题报告", description = "学生提交自己的开题报告")
    @PostMapping("/report")
    @PreAuthorize("hasAuthority('defense:report:submit')")
    public Result<String> submitReport(@Valid @RequestBody SubmitReportDTO dto) {
        String reportId = defenseService.submitReport(dto);
        return Result.success("提交成功", reportId);
    }

    @Operation(summary = "获取我的开题报告", description = "学生查看自己的开题报告")
    @GetMapping("/report/my")
    @PreAuthorize("hasAuthority('defense:report:my')")
    public Result<OpeningReportVO> getMyReport() {
        OpeningReportVO vo = defenseService.getMyReport();
        return Result.success(vo);
    }

    @Operation(summary = "分页查询开题报告列表", description = "企业教师查询学生的开题报告列表")
    @GetMapping("/report/page")
    @PreAuthorize("hasAuthority('defense:report:list')")
    public Result<IPage<OpeningReportVO>> pageReports(ReportQueryDTO queryDTO) {
        IPage<OpeningReportVO> page = defenseService.pageReports(queryDTO);
        return Result.success(page);
    }

    @Operation(summary = "获取开题报告详情", description = "根据报告ID获取详情")
    @GetMapping("/report/{reportId}")
    @PreAuthorize("hasAuthority('defense:report:detail')")
    public Result<OpeningReportVO> getReportDetail(
            @Parameter(description = "报告ID") @PathVariable String reportId) {
        OpeningReportVO vo = defenseService.getReportDetail(reportId);
        return Result.success(vo);
    }

    @Operation(summary = "审查开题报告", description = "企业教师审查学生的开题报告")
    @PostMapping("/report/review")
    @PreAuthorize("hasAuthority('defense:report:review')")
    public Result<Boolean> reviewReport(@Valid @RequestBody ReviewReportDTO dto) {
        Boolean success = defenseService.reviewReport(dto);
        return Result.success("审查完成", success);
    }
}

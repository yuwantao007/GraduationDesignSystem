package com.yuwan.completebackend.controller;

import com.yuwan.completebackend.common.PageResult;
import com.yuwan.completebackend.common.Result;
import com.yuwan.completebackend.common.annotation.PhaseRequired;
import com.yuwan.completebackend.model.dto.ApplyTopicDTO;
import com.yuwan.completebackend.model.dto.AssignTeacherDTO;
import com.yuwan.completebackend.model.enums.SystemPhase;
import com.yuwan.completebackend.model.vo.SelectionForTeacherVO;
import com.yuwan.completebackend.model.vo.SelectionForUnivTeacherVO;
import com.yuwan.completebackend.model.vo.SelectionOverviewVO;
import com.yuwan.completebackend.model.vo.UnivTeacherPairingVO;
import com.yuwan.completebackend.model.vo.TeacherAssignmentVO;
import com.yuwan.completebackend.model.vo.TopicForSelectionVO;
import com.yuwan.completebackend.model.vo.TopicSelectionVO;
import com.yuwan.completebackend.model.vo.UnselectedStudentVO;
import com.yuwan.completebackend.service.ITopicSelectionService;
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
 * 课题双选控制器
 * 提供学生选报、企业教师确认人选、企业负责人双选审核相关接口
 *
 * @author 系统架构师
 * @version 1.0
 * @since 2026-03-08
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/topic-selection")
@Tag(name = "课题双选", description = "课题双选全流程接口（学生选报 / 教师确认 / 负责人审核）")
public class TopicSelectionController {

    private final ITopicSelectionService topicSelectionService;

    // ==================== 学生选报 ====================

    /**
     * 查询可选课题列表（终审通过，分页）
     */
    @GetMapping("/available")
    @PreAuthorize("hasRole('STUDENT')")
    @Operation(summary = "可选课题列表", description = "学生查询终审通过的可选课题，支持按大类/方向/名称/专业筛选")
    public Result<PageResult<TopicForSelectionVO>> getAvailableTopics(
            @Parameter(description = "课题大类") @RequestParam(required = false) Integer topicCategory,
            @Parameter(description = "指导方向") @RequestParam(required = false) String guidanceDirection,
            @Parameter(description = "课题名称") @RequestParam(required = false) String topicTitle,
            @Parameter(description = "专业ID") @RequestParam(required = false) String majorId,
            @Parameter(description = "页码") @RequestParam(required = false, defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页条数") @RequestParam(required = false, defaultValue = "10") Integer pageSize) {
        return Result.success(topicSelectionService.getAvailableTopics(
                topicCategory, guidanceDirection, topicTitle, majorId, pageNum, pageSize));
    }

    /**
     * 学生选报课题（最多3个）
     */
    @PostMapping
    @PreAuthorize("hasRole('STUDENT')")
    @PhaseRequired(SystemPhase.TOPIC_SELECTION)
    @Operation(summary = "选报课题", description = "学生选报课题，最多选报3个，需先完善手机号")
    public Result<TopicSelectionVO> applyTopic(@Valid @RequestBody ApplyTopicDTO dto) {
        log.info("学生选报课题，课题ID: {}", dto.getTopicId());
        return Result.success(topicSelectionService.applyTopic(dto));
    }

    /**
     * 删除选报记录（仅落选后可删除）
     */
    @DeleteMapping("/{selectionId}")
    @PreAuthorize("hasRole('STUDENT')")
    @PhaseRequired(SystemPhase.TOPIC_SELECTION)
    @Operation(summary = "删除选报记录", description = "仅落选状态的记录可删除，以便重新选报")
    public Result<Void> deleteSelection(
            @Parameter(description = "选报记录ID") @PathVariable String selectionId) {
        log.info("学生删除选报记录，选报ID: {}", selectionId);
        topicSelectionService.deleteSelection(selectionId);
        return Result.success();
    }

    /**
     * 查询当前学生的选报记录
     */
    @GetMapping("/my")
    @PreAuthorize("hasRole('STUDENT')")
    @Operation(summary = "我的选报", description = "查询当前学生的课题选报记录列表")
    public Result<List<TopicSelectionVO>> getMySelections() {
        return Result.success(topicSelectionService.getMySelections());
    }

    // ==================== 教师确认子模块 ====================

    /**
     * 获取选报了自己课题的学生列表
     *
     * @param selectionStatus 状态过滤：0=待确认 1=中选 2=落选，不传则返回全部
     */
    @GetMapping("/teacher")
    @PreAuthorize("hasRole('ENTERPRISE_TEACHER')")
    @Operation(summary = "选报学生列表（教师视角）", description = "企业教师查看选报了自己课题的学生，可按状态筛选")
    public Result<List<SelectionForTeacherVO>> getSelectionsForTeacher(
            @Parameter(description = "选报状态（0-待确认 1-中选 2-落选），不传返回全部")
            @RequestParam(required = false) Integer selectionStatus) {
        return Result.success(topicSelectionService.getSelectionsForTeacher(selectionStatus));
    }

    /**
     * 确认人选（待确认 → 中选，并自动将该学生其他待确认记录落选）
     */
    @PostMapping("/{selectionId}/confirm")
    @PreAuthorize("hasRole('ENTERPRISE_TEACHER')")
    @PhaseRequired(SystemPhase.TOPIC_SELECTION)
    @Operation(summary = "确认人选", description = "确认学生中选，指导人数上限16人；确认后自动置落其他待确认记录")
    public Result<SelectionForTeacherVO> confirmSelection(
            @Parameter(description = "选报记录ID") @PathVariable String selectionId) {
        log.info("教师确认人选，选报ID: {}", selectionId);
        return Result.success(topicSelectionService.confirmSelection(selectionId));
    }

    /**
     * 拒绝人选（待确认 → 落选）
     */
    @PostMapping("/{selectionId}/reject")
    @PreAuthorize("hasRole('ENTERPRISE_TEACHER')")
    @PhaseRequired(SystemPhase.TOPIC_SELECTION)
    @Operation(summary = "拒绝人选", description = "拒绝学生选报，置为落选状态")
    public Result<SelectionForTeacherVO> rejectSelection(
            @Parameter(description = "选报记录ID") @PathVariable String selectionId) {
        log.info("教师拒绝人选，选报ID: {}", selectionId);
        return Result.success(topicSelectionService.rejectSelection(selectionId));
    }

    /**
     * 导出已确认学生信息（Excel）
     */
    @GetMapping("/teacher/export")
    @PreAuthorize("hasRole('ENTERPRISE_TEACHER')")
    @Operation(summary = "导出已确认学生", description = "企业教师导出已确认（中选）学生信息Excel")
    public void exportConfirmedStudents(HttpServletResponse response) throws Exception {
        byte[] bytes = topicSelectionService.exportConfirmedStudents();
        String filename = URLEncoder.encode("已确认学生信息.xls", StandardCharsets.UTF_8);
        response.setContentType("application/vnd.ms-excel");
        response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + filename);
        response.setContentLength(bytes.length);
        response.getOutputStream().write(bytes);
        response.getOutputStream().flush();
    }

    // ==================== 双选审核子模块 ====================

    /**
     * 获取双选结果概览（课题维度汇总）
     */
    @GetMapping("/leader/overview")
    @PreAuthorize("hasAnyRole('ENTERPRISE_LEADER', 'SYSTEM_ADMIN')")
    @Operation(summary = "双选结果概览", description = "企业负责人查看本企业所有课题的双选统计（选报/确认/待确认/落选人数）")
    public Result<List<SelectionOverviewVO>> getSelectionOverview() {
        return Result.success(topicSelectionService.getSelectionOverview());
    }

    /**
     * 获取未选报任何课题的学生列表
     */
    @GetMapping("/leader/unselected")
    @PreAuthorize("hasAnyRole('ENTERPRISE_LEADER', 'SYSTEM_ADMIN')")
    @Operation(summary = "未选报学生列表", description = "企业负责人查看本企业内尚未选报任何课题的学生")
    public Result<List<UnselectedStudentVO>> getUnselectedStudents() {
        return Result.success(topicSelectionService.getUnselectedStudents());
    }

    /**
     * 导出全部选题信息（Excel）
     */
    @GetMapping("/leader/export")
    @PreAuthorize("hasAnyRole('ENTERPRISE_LEADER', 'SYSTEM_ADMIN')")
    @Operation(summary = "导出选题信息", description = "企业负责人导出本企业全部课题双选概览Excel")
    public void exportSelectionInfo(HttpServletResponse response) throws Exception {
        byte[] bytes = topicSelectionService.exportSelectionInfo();
        String filename = URLEncoder.encode("课题双选结果.xls", StandardCharsets.UTF_8);
        response.setContentType("application/vnd.ms-excel");
        response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + filename);
        response.setContentLength(bytes.length);
        response.getOutputStream().write(bytes);
        response.getOutputStream().flush();
    }

    /**
     * 指派企业指导教师（校外协同开发课题中选学生）
     */
    @PostMapping("/leader/assign")
    @PreAuthorize("hasRole('ENTERPRISE_LEADER')")
    @PhaseRequired(SystemPhase.TOPIC_SELECTION)
    @Operation(summary = "指派教师", description = "为校外协同开发课题中选学生指派企业指导教师")
    public Result<TeacherAssignmentVO> assignTeacher(@Valid @RequestBody AssignTeacherDTO dto) {
        log.info("企业负责人指派教师，学生ID: {}，课题ID: {}", dto.getStudentId(), dto.getTopicId());
        return Result.success(topicSelectionService.assignTeacher(dto));
    }

    /**
     * 取消指派
     */
    @DeleteMapping("/leader/assign/{assignmentId}")
    @PreAuthorize("hasRole('ENTERPRISE_LEADER')")
    @PhaseRequired(SystemPhase.TOPIC_SELECTION)
    @Operation(summary = "取消指派", description = "取消已指派的教师记录")
    public Result<Void> cancelAssignment(
            @Parameter(description = "指派记录ID") @PathVariable String assignmentId) {
        log.info("企业负责人取消指派，指派ID: {}", assignmentId);
        topicSelectionService.cancelAssignment(assignmentId);
        return Result.success();
    }

    /**
     * 查询企业内指派记录列表
     */
    @GetMapping("/leader/assignments")
    @PreAuthorize("hasAnyRole('ENTERPRISE_LEADER', 'SYSTEM_ADMIN')")
    @Operation(summary = "指派记录列表", description = "企业负责人查看本企业的教师指派记录")
    public Result<List<TeacherAssignmentVO>> getAssignmentList() {
        return Result.success(topicSelectionService.getAssignmentList());
    }

    // ==================== 高校教师查看选题 ====================

    /**
     * 高校教师查询自身的配对关系（与是否有选报数据无关）
     */
    @GetMapping("/univ-teacher/pairings")
    @PreAuthorize("hasAnyRole('UNIVERSITY_TEACHER', 'SYSTEM_ADMIN')")
    @Operation(summary = "高校教师配对关系", description = "查询高校教师与企业教师的配对关系及课题/选报统计，配对存在即可查到，无需有选报数据")
    public Result<List<UnivTeacherPairingVO>> getUnivTeacherPairings() {
        return Result.success(topicSelectionService.getUnivTeacherPairings());
    }

    /**
     * 高校教师查看配对企业教师名下学生的选报结果
     *
     * @param selectionStatus 状态过滤：0=待确认 1=中选 2=落选，不传返回全部
     */
    @GetMapping("/univ-teacher")
    @PreAuthorize("hasAnyRole('UNIVERSITY_TEACHER', 'SYSTEM_ADMIN')")
    @Operation(summary = "指导学生选题结果（高校教师）",
            description = "高校教师通过配对关系查看企业教师名下所有学生的选报情况，支持按状态筛选")
    public Result<List<SelectionForUnivTeacherVO>> getSelectionsForUnivTeacher(
            @Parameter(description = "选报状态（0-待确认 1-中选 2-落选），不传返回全部")
            @RequestParam(required = false) Integer selectionStatus) {
        return Result.success(topicSelectionService.getSelectionsForUnivTeacher(selectionStatus));
    }

    /**
     * 高校教师导出指导学生选题结果（Excel）
     */
    @GetMapping("/univ-teacher/export")
    @PreAuthorize("hasRole('UNIVERSITY_TEACHER')")
    @Operation(summary = "导出选题结果（高校教师）", description = "高校教师导出配对企业教师名下学生选题情况Excel")
    public void exportSelectionsForUnivTeacher(HttpServletResponse response) throws Exception {
        byte[] bytes = topicSelectionService.exportSelectionsForUnivTeacher();
        String filename = URLEncoder.encode("指导学生选题结果.xls", StandardCharsets.UTF_8);
        response.setContentType("application/vnd.ms-excel");
        response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + filename);
        response.setContentLength(bytes.length);
        response.getOutputStream().write(bytes);
        response.getOutputStream().flush();
    }
}

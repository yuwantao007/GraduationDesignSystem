package com.yuwan.completebackend.controller;

import com.yuwan.completebackend.common.PageResult;
import com.yuwan.completebackend.common.Result;
import com.yuwan.completebackend.model.dto.BatchReviewDTO;
import com.yuwan.completebackend.model.dto.GeneralOpinionDTO;
import com.yuwan.completebackend.model.dto.ModifyReviewDTO;
import com.yuwan.completebackend.model.dto.ReviewTopicDTO;
import com.yuwan.completebackend.model.vo.*;
import com.yuwan.completebackend.security.SecurityUtil;
import com.yuwan.completebackend.service.ITopicReviewService;
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
 * 课题审查控制器
 * 提供课题审查相关的API接口
 * 
 * <p>审查流程说明：</p>
 * <ul>
 *   <li>高职升本课题：预审(高校教师) → 初审(专业方向主管) → 终审(督导教师)</li>
 *   <li>3+1/实验班课题：初审(专业方向主管) → 终审(高校教师)</li>
 * </ul>
 *
 * @author 系统架构师
 * @version 1.1
 * @since 2026-02-23
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/topic/review")
@Tag(name = "课题审查管理", description = "课题审查流程相关接口（预审/初审/终审）")
public class TopicReviewController {

    private final ITopicReviewService topicReviewService;

    /**
     * 获取待审查课题列表
     *
     * @param queryVO 查询参数
     * @return 待审查课题分页列表
     */
    @GetMapping("/pending")
    @PreAuthorize("hasAnyRole('UNIVERSITY_TEACHER', 'MAJOR_DIRECTOR', 'SUPERVISOR_TEACHER')")
    @Operation(summary = "获取待审查课题列表", description = "根据当前用户角色自动筛选待审查的课题列表")
    public Result<PageResult<TopicReviewListVO>> getPendingTopics(ReviewQueryVO queryVO) {
        log.info("获取待审查课题列表，页码: {}, 页大小: {}", queryVO.getPageNum(), queryVO.getPageSize());
        PageResult<TopicReviewListVO> result = topicReviewService.getPendingTopics(queryVO);
        return Result.success(result);
    }

    /**
     * 单个课题审批
     *
     * @param reviewDTO 审批请求参数
     * @return 审批后的课题信息
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('UNIVERSITY_TEACHER', 'MAJOR_DIRECTOR', 'SUPERVISOR_TEACHER')")
    @Operation(summary = "单个课题审批", description = "对单个课题进行审批（通过/需修改/不通过）")
    public Result<TopicVO> reviewTopic(@Valid @RequestBody ReviewTopicDTO reviewDTO) {
        log.info("单个课题审批，课题ID: {}, 审批结果: {}", reviewDTO.getTopicId(), reviewDTO.getReviewResult());
        TopicVO result = topicReviewService.reviewTopic(reviewDTO);
        return Result.success(result);
    }

    /**
     * 批量课题审批
     *
     * @param batchDTO 批量审批请求参数
     * @return 批量审批结果
     */
    @PostMapping("/batch")
    @PreAuthorize("hasAnyRole('UNIVERSITY_TEACHER', 'MAJOR_DIRECTOR', 'SUPERVISOR_TEACHER')")
    @Operation(summary = "批量课题审批", description = "批量对多个课题进行审批（通过/需修改/不通过）")
    public Result<BatchReviewResultVO> batchReviewTopics(@Valid @RequestBody BatchReviewDTO batchDTO) {
        log.info("批量课题审批，课题数量: {}, 审批结果: {}", batchDTO.getTopicIds().size(), batchDTO.getReviewResult());
        BatchReviewResultVO result = topicReviewService.batchReviewTopics(batchDTO);
        return Result.success(result);
    }

    /**
     * 获取课题审查历史记录
     *
     * @param topicId 课题ID
     * @return 审查历史记录列表
     */
    @GetMapping("/{topicId}/history")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN', 'ENTERPRISE_TEACHER', 'UNIVERSITY_TEACHER', 'MAJOR_DIRECTOR', 'SUPERVISOR_TEACHER')")
    @Operation(summary = "获取课题审查历史", description = "获取指定课题的所有审查历史记录")
    public Result<List<TopicReviewRecordVO>> getReviewHistory(
            @Parameter(description = "课题ID") @PathVariable String topicId) {
        log.info("获取课题审查历史，课题ID: {}", topicId);
        List<TopicReviewRecordVO> result = topicReviewService.getReviewHistory(topicId);
        return Result.success(result);
    }

    /**
     * 提交综合修改意见
     *
     * @param opinionDTO 综合意见请求参数
     * @return 综合意见信息
     */
    @PostMapping("/general-opinion")
    @PreAuthorize("hasAnyRole('MAJOR_DIRECTOR', 'SUPERVISOR_TEACHER')")
    @Operation(summary = "提交综合修改意见", description = "提交针对某专业方向的综合修改意见（本专业方向所有教师可见）")
    public Result<GeneralOpinionVO> submitGeneralOpinion(@Valid @RequestBody GeneralOpinionDTO opinionDTO) {
        log.info("提交综合意见，专业方向: {}, 阶段: {}", opinionDTO.getGuidanceDirection(), opinionDTO.getReviewStage());
        GeneralOpinionVO result = topicReviewService.submitGeneralOpinion(opinionDTO);
        return Result.success(result);
    }

    /**
     * 获取综合意见列表
     *
     * @param reviewStage       审查阶段（可选）
     * @param guidanceDirection 专业方向（可选，不传则查询所有）
     * @return 综合意见列表
     */
    @GetMapping("/general-opinions")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN', 'ENTERPRISE_TEACHER', 'UNIVERSITY_TEACHER', 'MAJOR_DIRECTOR', 'SUPERVISOR_TEACHER')")
    @Operation(summary = "获取综合意见列表", description = "获取综合修改意见列表，可按专业方向筛选")
    public Result<List<GeneralOpinionVO>> getGeneralOpinions(
            @Parameter(description = "审查阶段（1-预审 2-初审 3-终审）") @RequestParam(required = false) Integer reviewStage,
            @Parameter(description = "专业方向（不传则查询所有）") @RequestParam(required = false) String guidanceDirection) {
        log.info("获取综合意见列表，专业方向: {}, 阶段: {}", guidanceDirection, reviewStage);
        List<GeneralOpinionVO> result = topicReviewService.getGeneralOpinions(reviewStage, guidanceDirection);
        return Result.success(result);
    }

    /**
     * 修改审查结果
     *
     * @param modifyDTO 修改审查请求参数
     * @return 修改后的课题信息
     */
    @PutMapping("/modify")
    @PreAuthorize("hasAnyRole('UNIVERSITY_TEACHER', 'MAJOR_DIRECTOR', 'SUPERVISOR_TEACHER')")
    @Operation(summary = "修改审查结果", description = "修改自己的审查结果（需满足下级未通过的条件）")
    public Result<TopicVO> modifyReviewResult(@Valid @RequestBody ModifyReviewDTO modifyDTO) {
        log.info("修改审查结果，审查记录ID: {}, 新结果: {}", modifyDTO.getReviewId(), modifyDTO.getNewReviewResult());
        TopicVO result = topicReviewService.modifyReviewResult(modifyDTO);
        return Result.success(result);
    }

    /**
     * 删除综合意见
     *
     * @param opinionId 意见ID
     * @return 操作结果
     */
    @DeleteMapping("/general-opinion/{opinionId}")
    @PreAuthorize("hasAnyRole('MAJOR_DIRECTOR', 'SUPERVISOR_TEACHER')")
    @Operation(summary = "删除综合意见", description = "删除自己提交的综合意见")
    public Result<Void> deleteGeneralOpinion(
            @Parameter(description = "意见ID") @PathVariable String opinionId) {
        log.info("删除综合意见，意见ID: {}", opinionId);
        topicReviewService.deleteGeneralOpinion(opinionId);
        return Result.success();
    }

    /**
     * 获取教师通过终审的课题数统计
     *
     * @param teacherId 教师ID（可选，不传则查询当前登录用户）
     * @return 统计信息
     */
    @GetMapping("/stats/passed-count")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN', 'ENTERPRISE_LEADER', 'SUPERVISOR_TEACHER', 'ENTERPRISE_TEACHER')")
    @Operation(summary = "获取教师通过终审课题统计", description = "统计指定企业教师通过终审的课题数量及剩余可提交数，不传teacherId则查询当前用户")
    public Result<TeacherPassedCountVO> getTeacherPassedCount(
            @Parameter(description = "教师ID（企业教师，不传则查询当前用户）") @RequestParam(required = false) String teacherId) {
        // 如果不传teacherId，则使用当前登录用户ID
        if (teacherId == null || teacherId.isEmpty()) {
            teacherId = SecurityUtil.getCurrentUserId();
        }
        log.info("获取教师通过终审课题统计，教师ID: {}", teacherId);
        TeacherPassedCountVO result = topicReviewService.getTeacherPassedCount(teacherId);
        return Result.success(result);
    }

    /**
     * 检查教师是否可以继续提交课题
     *
     * @param teacherId 教师ID
     * @return 是否可提交
     */
    @GetMapping("/check-submit")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN', 'ENTERPRISE_TEACHER')")
    @Operation(summary = "检查教师是否可提交课题", description = "检查企业教师是否已达到18个课题的上限")
    public Result<Boolean> canTeacherSubmitTopic(
            @Parameter(description = "教师ID") @RequestParam String teacherId) {
        log.info("检查教师是否可提交课题，教师ID: {}", teacherId);
        boolean canSubmit = topicReviewService.canTeacherSubmitTopic(teacherId);
        return Result.success(canSubmit);
    }

    /**
     * 检查当前用户是否可以审查指定课题
     *
     * @param topicId 课题ID
     * @return 是否可审查
     */
    @GetMapping("/{topicId}/can-review")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN', 'UNIVERSITY_TEACHER', 'MAJOR_DIRECTOR', 'SUPERVISOR_TEACHER')")
    @Operation(summary = "检查是否可审查课题", description = "检查当前用户是否有权限审查指定课题")
    public Result<Boolean> canReviewTopic(
            @Parameter(description = "课题ID") @PathVariable String topicId) {
        log.info("检查是否可审查课题，课题ID: {}", topicId);
        boolean canReview = topicReviewService.canReviewTopic(topicId);
        return Result.success(canReview);
    }

    /**
     * 获取当前用户可修改的审查记录
     *
     * @param topicId 课题ID
     * @return 可修改的审查记录
     */
    @GetMapping("/{topicId}/modifiable-record")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN', 'UNIVERSITY_TEACHER', 'MAJOR_DIRECTOR', 'SUPERVISOR_TEACHER')")
    @Operation(summary = "获取可修改的审查记录", description = "获取当前用户对指定课题可修改的审查记录")
    public Result<TopicReviewRecordVO> getModifiableReviewRecord(
            @Parameter(description = "课题ID") @PathVariable String topicId) {
        log.info("获取可修改的审查记录，课题ID: {}", topicId);
        TopicReviewRecordVO result = topicReviewService.getModifiableReviewRecord(topicId);
        return Result.success(result);
    }
}

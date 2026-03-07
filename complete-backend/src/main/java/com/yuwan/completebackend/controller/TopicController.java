package com.yuwan.completebackend.controller;

import com.yuwan.completebackend.common.PageResult;
import com.yuwan.completebackend.common.Result;
import com.yuwan.completebackend.common.annotation.PhaseRequired;
import com.yuwan.completebackend.model.enums.SystemPhase;
import com.yuwan.completebackend.model.dto.CreateTopicDTO;
import com.yuwan.completebackend.model.dto.SubmitTopicDTO;
import com.yuwan.completebackend.model.dto.TopicSignDTO;
import com.yuwan.completebackend.model.dto.UpdateTopicDTO;
import com.yuwan.completebackend.model.vo.TopicListVO;
import com.yuwan.completebackend.model.vo.TopicQueryVO;
import com.yuwan.completebackend.model.vo.TopicVO;
import com.yuwan.completebackend.service.ITopicService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 课题管理控制器
 * 提供课题申报的CRUD、提交、撤回、签名等接口
 *
 * @author 系统架构师
 * @version 1.0
 * @since 2026-02-21
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/topic")
@Tag(name = "课题管理", description = "课题申报CRUD、提交、签名等接口")
public class TopicController {

    private final ITopicService topicService;

    /**
     * 创建课题
     *
     * @param createDTO 创建课题请求参数
     * @return 课题信息
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN', 'ENTERPRISE_TEACHER')")
    @PhaseRequired(SystemPhase.TOPIC_DECLARATION)
    @Operation(summary = "创建课题", description = "企业教师创建新课题（保存为草稿）")
    public Result<TopicVO> createTopic(@Valid @RequestBody CreateTopicDTO createDTO) {
        log.info("创建课题请求，课题名称: {}", createDTO.getTopicTitle());
        TopicVO result = topicService.createTopic(createDTO);
        return Result.success(result);
    }

    /**
     * 更新课题
     *
     * @param topicId 课题ID
     * @param updateDTO 更新课题请求参数
     * @return 课题信息
     */
    @PutMapping("/{topicId}")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN', 'ENTERPRISE_TEACHER')")
    @PhaseRequired(SystemPhase.TOPIC_DECLARATION)
    @Operation(summary = "更新课题", description = "更新课题内容，仅创建人可更新")
    public Result<TopicVO> updateTopic(
            @Parameter(description = "课题ID") @PathVariable String topicId,
            @Valid @RequestBody UpdateTopicDTO updateDTO) {
        log.info("更新课题请求，课题ID: {}", topicId);
        TopicVO result = topicService.updateTopic(topicId, updateDTO);
        return Result.success(result);
    }

    /**
     * 获取课题详情
     *
     * @param topicId 课题ID
     * @return 课题信息
     */
    @GetMapping("/{topicId}")
    @Operation(summary = "获取课题详情", description = "根据ID获取课题详细信息")
    public Result<TopicVO> getTopicDetail(
            @Parameter(description = "课题ID") @PathVariable String topicId) {
        log.info("获取课题详情，课题ID: {}", topicId);
        TopicVO result = topicService.getTopicDetail(topicId);
        return Result.success(result);
    }

    /**
     * 分页查询课题列表
     *
     * @param queryVO 查询参数
     * @return 分页课题列表
     */
    @GetMapping("/list")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN', 'ENTERPRISE_TEACHER', 'UNIVERSITY_TEACHER', 'MAJOR_DIRECTOR', 'SUPERVISOR_TEACHER')")
    @Operation(summary = "分页查询课题列表", description = "根据条件分页查询课题列表（管理员和教师可查看）")
    public Result<PageResult<TopicListVO>> getTopicList(TopicQueryVO queryVO) {
        log.info("分页查询课题列表，页码: {}, 页大小: {}", queryVO.getPageNum(), queryVO.getPageSize());
        PageResult<TopicListVO> result = topicService.getTopicList(queryVO);
        return Result.success(result);
    }

    /**
     * 获取我的课题列表
     *
     * @param queryVO 查询参数
     * @return 分页课题列表
     */
    @GetMapping("/my")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN', 'ENTERPRISE_TEACHER')")
    @Operation(summary = "获取我的课题列表", description = "获取当前用户创建的课题列表")
    public Result<PageResult<TopicListVO>> getMyTopics(TopicQueryVO queryVO) {
        log.info("获取我的课题列表，页码: {}, 页大小: {}", queryVO.getPageNum(), queryVO.getPageSize());
        PageResult<TopicListVO> result = topicService.getMyTopics(queryVO);
        return Result.success(result);
    }

    /**
     * 删除课题
     *
     * @param topicId 课题ID
     * @return 操作结果
     */
    @DeleteMapping("/{topicId}")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN', 'ENTERPRISE_TEACHER')")
    @PhaseRequired(SystemPhase.TOPIC_DECLARATION)
    @Operation(summary = "删除课题", description = "删除课题（仅草稿状态可删除）")
    public Result<Void> deleteTopic(
            @Parameter(description = "课题ID") @PathVariable String topicId) {
        log.info("删除课题请求，课题ID: {}", topicId);
        topicService.deleteTopic(topicId);
        return Result.success();
    }

    /**
     * 提交课题
     *
     * @param submitDTO 提交请求参数
     * @return 课题信息
     */
    @PostMapping("/submit")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN', 'ENTERPRISE_TEACHER')")
    @PhaseRequired(SystemPhase.TOPIC_DECLARATION)
    @Operation(summary = "提交课题", description = "提交课题申报（草稿→待预审）")
    public Result<TopicVO> submitTopic(@Valid @RequestBody SubmitTopicDTO submitDTO) {
        log.info("提交课题请求，课题ID: {}", submitDTO.getTopicId());
        TopicVO result = topicService.submitTopic(submitDTO);
        return Result.success(result);
    }

    /**
     * 撤回课题
     *
     * @param topicId 课题ID
     * @return 课题信息
     */
    @PostMapping("/{topicId}/withdraw")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN', 'ENTERPRISE_TEACHER')")
    @PhaseRequired(SystemPhase.TOPIC_DECLARATION)
    @Operation(summary = "撤回课题", description = "撤回已提交的课题（仅待预审状态可撤回）")
    public Result<TopicVO> withdrawTopic(
            @Parameter(description = "课题ID") @PathVariable String topicId) {
        log.info("撤回课题请求，课题ID: {}", topicId);
        TopicVO result = topicService.withdrawTopic(topicId);
        return Result.success(result);
    }

    /**
     * 课题签名
     *
     * @param signDTO 签名请求参数
     * @return 课题信息
     */
    @PostMapping("/sign")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN', 'ENTERPRISE_TEACHER', 'ENTERPRISE_LEADER', 'UNIVERSITY_TEACHER')")
    @PhaseRequired(SystemPhase.TOPIC_DECLARATION)
    @Operation(summary = "课题签名", description = "对课题进行电子签名")
    public Result<TopicVO> signTopic(@Valid @RequestBody TopicSignDTO signDTO) {
        log.info("课题签名请求，课题ID: {}, 签名类型: {}", signDTO.getTopicId(), signDTO.getSignType());
        TopicVO result = topicService.signTopic(signDTO);
        return Result.success(result);
    }

    /**
     * 统计通过终审的课题数
     *
     * @param creatorId 创建人ID
     * @return 通过终审的课题数量
     */
    @GetMapping("/count/passed")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN', 'ENTERPRISE_LEADER')")
    @Operation(summary = "统计通过终审的课题数", description = "统计指定用户通过终审的课题数量")
    public Result<Integer> countPassedTopics(
            @Parameter(description = "创建人ID") @RequestParam String creatorId) {
        log.info("统计通过终审的课题数，创建人ID: {}", creatorId);
        int count = topicService.countPassedTopics(creatorId);
        return Result.success(count);
    }
}

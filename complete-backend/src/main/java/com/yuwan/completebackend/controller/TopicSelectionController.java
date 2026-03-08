package com.yuwan.completebackend.controller;

import com.yuwan.completebackend.common.PageResult;
import com.yuwan.completebackend.common.Result;
import com.yuwan.completebackend.common.annotation.PhaseRequired;
import com.yuwan.completebackend.model.dto.ApplyTopicDTO;
import com.yuwan.completebackend.model.enums.SystemPhase;
import com.yuwan.completebackend.model.vo.TopicForSelectionVO;
import com.yuwan.completebackend.model.vo.TopicSelectionVO;
import com.yuwan.completebackend.service.ITopicSelectionService;
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
 * 课题选报控制器
 * 提供学生课题选报的相关接口
 *
 * @author 系统架构师
 * @version 1.0
 * @since 2026-03-08
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/topic-selection")
@Tag(name = "课题选报", description = "学生课题选报相关接口")
public class TopicSelectionController {

    private final ITopicSelectionService topicSelectionService;

    /**
     * 查询可选课题列表（终审通过的课题，分页）
     *
     * @param topicCategory     课题大类筛选
     * @param guidanceDirection 指导方向模糊搜索
     * @param topicTitle        课题名称模糊搜索
     * @param majorId           专业ID筛选
     * @param pageNum           页码
     * @param pageSize          每页条数
     * @return 分页课题列表
     */
    @GetMapping("/available")
    @PreAuthorize("hasRole('STUDENT')")
    @Operation(summary = "查询可选课题列表", description = "学生查询终审通过的可选课题列表")
    public Result<PageResult<TopicForSelectionVO>> getAvailableTopics(
            @Parameter(description = "课题大类") @RequestParam(required = false) Integer topicCategory,
            @Parameter(description = "指导方向") @RequestParam(required = false) String guidanceDirection,
            @Parameter(description = "课题名称") @RequestParam(required = false) String topicTitle,
            @Parameter(description = "专业ID") @RequestParam(required = false) String majorId,
            @Parameter(description = "页码") @RequestParam(required = false, defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页条数") @RequestParam(required = false, defaultValue = "10") Integer pageSize) {
        PageResult<TopicForSelectionVO> result = topicSelectionService.getAvailableTopics(
                topicCategory, guidanceDirection, topicTitle, majorId, pageNum, pageSize
        );
        return Result.success(result);
    }

    /**
     * 学生选报课题
     *
     * @param dto 选报参数
     * @return 选报记录
     */
    @PostMapping
    @PreAuthorize("hasRole('STUDENT')")
    @PhaseRequired(SystemPhase.TOPIC_SELECTION)
    @Operation(summary = "选报课题", description = "学生选报课题，最多选报3个")
    public Result<TopicSelectionVO> applyTopic(@Valid @RequestBody ApplyTopicDTO dto) {
        log.info("学生选报课题，课题ID: {}", dto.getTopicId());
        TopicSelectionVO result = topicSelectionService.applyTopic(dto);
        return Result.success(result);
    }

    /**
     * 删除选报记录（仅落选后可删除）
     *
     * @param selectionId 选报记录ID
     * @return 操作结果
     */
    @DeleteMapping("/{selectionId}")
    @PreAuthorize("hasRole('STUDENT')")
    @PhaseRequired(SystemPhase.TOPIC_SELECTION)
    @Operation(summary = "删除选报记录", description = "删除落选的选报记录，以便重新选报")
    public Result<Void> deleteSelection(
            @Parameter(description = "选报记录ID") @PathVariable String selectionId) {
        log.info("学生删除选报记录，选报ID: {}", selectionId);
        topicSelectionService.deleteSelection(selectionId);
        return Result.success();
    }

    /**
     * 查询当前学生的选报记录列表
     *
     * @return 选报记录列表
     */
    @GetMapping("/my")
    @PreAuthorize("hasRole('STUDENT')")
    @Operation(summary = "我的选报", description = "查询当前学生的课题选报记录")
    public Result<List<TopicSelectionVO>> getMySelections() {
        List<TopicSelectionVO> result = topicSelectionService.getMySelections();
        return Result.success(result);
    }
}

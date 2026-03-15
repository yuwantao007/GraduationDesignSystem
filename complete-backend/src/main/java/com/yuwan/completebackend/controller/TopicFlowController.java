package com.yuwan.completebackend.controller;

import com.yuwan.completebackend.common.PageResult;
import com.yuwan.completebackend.common.Result;
import com.yuwan.completebackend.model.dto.CompleteReviewTaskDTO;
import com.yuwan.completebackend.model.vo.FlowTaskVO;
import com.yuwan.completebackend.model.vo.ProcessInstanceVO;
import com.yuwan.completebackend.model.vo.ProcessStatusVO;
import com.yuwan.completebackend.security.SecurityUtil;
import com.yuwan.completebackend.service.ITopicFlowService;
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
 * 课题审查流程控制器
 * <p>
 * 提供 Flowable 工作流的任务管理、流程状态查询、历史记录等 REST 接口。
 * </p>
 *
 * @author 系统架构师
 * @version 1.0
 * @since 2026-03-15
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/flow")
@Tag(name = "课题审查流程", description = "Flowable 流程引擎 - 任务管理与流程监控接口")
public class TopicFlowController {

    private final ITopicFlowService topicFlowService;

    /**
     * 获取当前用户的待办任务列表
     * <p>
     * 审核角色（高校教师/专业方向主管/督导教师）查询属于自己的待审核任务；
     * 企业教师查询自己的修改待重提任务。
     * </p>
     */
    @GetMapping("/task/my")
    @PreAuthorize("hasAnyAuthority('topic:review:pre', 'topic:review:init', 'topic:review:final', " +
            "'topic:create', 'monitor:dashboard:view')")
    @Operation(summary = "我的待办任务", description = "查询当前用户的待签收/已签收待办任务列表")
    public Result<List<FlowTaskVO>> getMyTasks() {
        String userId = SecurityUtil.getCurrentUserId();
        List<String> roles = SecurityUtil.getCurrentUserRoles();
        // 取第一个业务角色（去掉 ROLE_ 前缀）
        String roleCode = roles.stream()
                .map(r -> r.startsWith("ROLE_") ? r.substring(5) : r)
                .findFirst()
                .orElse("");
        return Result.success(topicFlowService.getMyTasks(userId, roleCode));
    }

    /**
     * 签收任务
     */
    @PostMapping("/task/{taskId}/claim")
    @PreAuthorize("hasAnyAuthority('topic:review:pre', 'topic:review:init', 'topic:review:final')")
    @Operation(summary = "签收任务", description = "将候选任务签收到当前用户，签收后其他人不可操作")
    public Result<Void> claimTask(
            @Parameter(description = "任务ID") @PathVariable String taskId) {
        String userId = SecurityUtil.getCurrentUserId();
        topicFlowService.claimTask(taskId, userId);
        return Result.success();
    }

    /**
     * 完成审查任务（提交审核意见）
     */
    @PostMapping("/task/{taskId}/complete")
    @PreAuthorize("hasAnyAuthority('topic:review:pre', 'topic:review:init', 'topic:review:final')")
    @Operation(summary = "完成审查任务", description = "提交审核意见并路由到下一节点，outcome: PASS/NEED_MODIFY/REJECT")
    public Result<Void> completeReviewTask(
            @Parameter(description = "任务ID") @PathVariable String taskId,
            @Valid @RequestBody CompleteReviewTaskDTO dto) {
        String userId = SecurityUtil.getCurrentUserId();
        topicFlowService.completeReviewTask(taskId, dto.getOutcome(), dto.getOpinion(), userId);
        return Result.success();
    }

    /**
     * 获取课题当前流程状态（含历史节点）
     */
    @GetMapping("/process/topic/{topicId}")
    @Operation(summary = "课题流程状态", description = "返回课题的流程当前节点、历史节点和活跃任务，用于课题详情页展示")
    public Result<ProcessStatusVO> getProcessStatus(
            @Parameter(description = "课题ID") @PathVariable String topicId) {
        return Result.success(topicFlowService.getProcessStatus(topicId));
    }

    /**
     * 管理员分页查询所有流程实例
     */
    @GetMapping("/process/list")
    @PreAuthorize("hasAuthority('monitor:dashboard:view')")
    @Operation(summary = "流程实例监控列表", description = "管理员查看所有课题审查流程实例，支持按状态过滤")
    public Result<PageResult<ProcessInstanceVO>> listProcessInstances(
            @Parameter(description = "流程状态（0-运行中 1-已完成 2-已终止，不传则查全部）")
            @RequestParam(required = false) Integer processStatus,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        return Result.success(topicFlowService.listProcessInstances(processStatus, pageNum, pageSize));
    }

    /**
     * 获取课题审查流程定义的 BPMN XML（不依赖实例，用于流程定义可视化）
     * <p>
     * 无需管理员权限，所有已登录用户均可查看流程定义图，便于了解审查路径。
     * </p>
     */
    @GetMapping("/definition/diagram")
    @Operation(summary = "获取流程定义BPMN图",
            description = "返回 topic_review 最新版本流程定义的 BPMN 2.0 XML，用于流程定义可视化页面展示")
    public Result<String> getDefinitionDiagram() {
        return Result.success(topicFlowService.getProcessDefinitionDiagramXml());
    }

    /**
     * 获取流程实例的 BPMN XML（前端 bpmn-js 渲染用）
     */
    @GetMapping("/process/{processInstanceId}/diagram")
    @PreAuthorize("hasAuthority('monitor:dashboard:view')")
    @Operation(summary = "获取 BPMN 流程图 XML",
            description = "返回 BPMN 2.0 XML 字符串，前端 bpmn-js 解析渲染，配合高亮活跃节点使用")
    public Result<String> getProcessDiagram(
            @Parameter(description = "流程实例ID") @PathVariable String processInstanceId) {
        return Result.success(topicFlowService.getProcessDiagramXml(processInstanceId));
    }

    /**
     * 获取流程实例的历史节点记录
     */
    @GetMapping("/process/{processInstanceId}/history")
    @PreAuthorize("hasAnyAuthority('topic:review:pre', 'topic:review:init', 'topic:review:final', " +
            "'monitor:dashboard:view')")
    @Operation(summary = "流程历史记录", description = "返回流程实例的所有历史节点（含已完成和当前活跃节点），用于时间线展示")
    public Result<List<ProcessStatusVO.HistoryNodeVO>> getProcessHistory(
            @Parameter(description = "流程实例ID") @PathVariable String processInstanceId) {
        return Result.success(topicFlowService.getProcessHistory(processInstanceId));
    }
}

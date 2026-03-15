package com.yuwan.completebackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yuwan.completebackend.common.PageResult;
import com.yuwan.completebackend.exception.BusinessException;
import com.yuwan.completebackend.mapper.TopicMapper;
import com.yuwan.completebackend.mapper.TopicProcessInstanceMapper;
import com.yuwan.completebackend.mapper.TopicReviewRecordMapper;
import com.yuwan.completebackend.mapper.UserMapper;
import com.yuwan.completebackend.model.entity.Topic;
import com.yuwan.completebackend.model.entity.TopicProcessInstance;
import com.yuwan.completebackend.model.entity.TopicReviewRecord;
import com.yuwan.completebackend.model.entity.User;
import com.yuwan.completebackend.model.enums.ReviewResult;
import com.yuwan.completebackend.model.enums.ReviewStage;
import com.yuwan.completebackend.model.enums.TopicCategory;
import com.yuwan.completebackend.model.enums.TopicReviewStatus;
import com.yuwan.completebackend.model.vo.FlowTaskVO;
import com.yuwan.completebackend.model.vo.ProcessInstanceVO;
import com.yuwan.completebackend.model.vo.ProcessStatusVO;
import com.yuwan.completebackend.service.ITopicFlowService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.*;
import org.flowable.engine.history.HistoricActivityInstance;
import org.flowable.engine.history.HistoricProcessInstance;
import org.flowable.engine.repository.ProcessDefinition;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.task.api.Task;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 课题审查流程服务实现类
 * <p>
 * 封装 Flowable 流程引擎的核心操作，负责流程启动、任务签收与完成、
 * 状态同步（reviewStatus）以及流程历史查询。
 * </p>
 *
 * @author 系统架构师
 * @version 1.0
 * @since 2026-03-15
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
public class TopicFlowServiceImpl implements ITopicFlowService {

    private static final String PROCESS_DEF_KEY = "topic_review";

    /** 任务定义Key → 任务完成后对应的 reviewStatus 映射（PASS 结果） */
    private static final Map<String, Integer> TASK_PASS_STATUS_MAP;
    /** 任务定义Key → 任务完成后对应的 reviewStatus 映射（NEED_MODIFY 结果） */
    private static final Map<String, Integer> TASK_MODIFY_STATUS_MAP;
    /** 任务定义Key → 对应的 ReviewStage */
    private static final Map<String, ReviewStage> TASK_STAGE_MAP;
    /** 修改任务的任务Key集合 */
    private static final Set<String> MODIFY_TASK_KEYS;
    /** 任务Key → 候选角色描述 */
    private static final Map<String, String> TASK_ROLE_DESC_MAP;

    static {
        TASK_PASS_STATUS_MAP = new HashMap<>();
        TASK_PASS_STATUS_MAP.put("preReviewTask",       TopicReviewStatus.PRE_PASSED.getCode());
        TASK_PASS_STATUS_MAP.put("initUpgradeTask",     TopicReviewStatus.INIT_PASSED.getCode());
        TASK_PASS_STATUS_MAP.put("initOtherTask",       TopicReviewStatus.INIT_PASSED.getCode());
        TASK_PASS_STATUS_MAP.put("finalUpgradeTask",    TopicReviewStatus.FINAL_PASSED.getCode());
        TASK_PASS_STATUS_MAP.put("finalOtherTask",      TopicReviewStatus.FINAL_PASSED.getCode());

        TASK_MODIFY_STATUS_MAP = new HashMap<>();
        TASK_MODIFY_STATUS_MAP.put("preReviewTask",     TopicReviewStatus.PRE_MODIFY.getCode());
        TASK_MODIFY_STATUS_MAP.put("initUpgradeTask",   TopicReviewStatus.INIT_MODIFY.getCode());
        TASK_MODIFY_STATUS_MAP.put("initOtherTask",     TopicReviewStatus.INIT_MODIFY.getCode());

        TASK_STAGE_MAP = new HashMap<>();
        TASK_STAGE_MAP.put("preReviewTask",      ReviewStage.PRE_REVIEW);
        TASK_STAGE_MAP.put("initUpgradeTask",    ReviewStage.INIT_REVIEW);
        TASK_STAGE_MAP.put("initOtherTask",      ReviewStage.INIT_REVIEW);
        TASK_STAGE_MAP.put("finalUpgradeTask",   ReviewStage.FINAL_REVIEW);
        TASK_STAGE_MAP.put("finalOtherTask",     ReviewStage.FINAL_REVIEW);

        MODIFY_TASK_KEYS = new HashSet<>(Arrays.asList(
                "preModifyTask", "initUpgradeModifyTask", "initOtherModifyTask"));

        TASK_ROLE_DESC_MAP = new HashMap<>();
        TASK_ROLE_DESC_MAP.put("preReviewTask",         "高校教师（预审）");
        TASK_ROLE_DESC_MAP.put("initUpgradeTask",       "专业方向主管（初审）");
        TASK_ROLE_DESC_MAP.put("initOtherTask",         "专业方向主管（初审）");
        TASK_ROLE_DESC_MAP.put("finalUpgradeTask",      "督导教师（终审）");
        TASK_ROLE_DESC_MAP.put("finalOtherTask",        "高校教师（终审）");
        TASK_ROLE_DESC_MAP.put("preModifyTask",         "企业教师（修改待重提）");
        TASK_ROLE_DESC_MAP.put("initUpgradeModifyTask", "企业教师（修改待重提）");
        TASK_ROLE_DESC_MAP.put("initOtherModifyTask",   "企业教师（修改待重提）");
    }

    private final RuntimeService runtimeService;
    private final TaskService taskService;
    private final HistoryService historyService;
    private final RepositoryService repositoryService;

    private final TopicMapper topicMapper;
    private final UserMapper userMapper;
    private final TopicProcessInstanceMapper processInstanceMapper;
    private final TopicReviewRecordMapper reviewRecordMapper;

    // ======================== 流程启动 ========================

    @Override
    public String startProcess(String topicId, String creatorId, Integer topicCategory, String topicTitle) {
        // 检查是否已存在进行中的流程实例
        TopicProcessInstance existing = processInstanceMapper.selectOne(
                new LambdaQueryWrapper<TopicProcessInstance>()
                        .eq(TopicProcessInstance::getTopicId, topicId)
                        .eq(TopicProcessInstance::getDeleted, 0));
        if (existing != null && existing.getProcessStatus() == 0) {
            log.warn("[Flow] 课题 {} 已存在运行中的流程实例 {}，跳过重复启动",
                    topicId, existing.getProcessInstanceId());
            return existing.getProcessInstanceId();
        }

        Map<String, Object> variables = new HashMap<>();
        variables.put("topicId", topicId);
        variables.put("creatorId", creatorId);
        variables.put("topicCategory", topicCategory);
        variables.put("topicTitle", topicTitle != null ? topicTitle : "");

        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(
                PROCESS_DEF_KEY, topicId, variables);

        // 保存映射关系
        TopicProcessInstance mapping = new TopicProcessInstance();
        mapping.setTopicId(topicId);
        mapping.setProcessInstanceId(processInstance.getId());
        mapping.setProcessDefKey(PROCESS_DEF_KEY);
        mapping.setTopicCategory(topicCategory);
        mapping.setProcessStatus(0);

        if (existing != null) {
            // 旧记录更新
            existing.setProcessInstanceId(processInstance.getId());
            existing.setProcessStatus(0);
            processInstanceMapper.updateById(existing);
        } else {
            processInstanceMapper.insert(mapping);
        }

        log.info("[Flow] 启动课题审查流程 - 课题ID: {}, 流程实例ID: {}, 大类: {}",
                topicId, processInstance.getId(), topicCategory);
        return processInstance.getId();
    }

    // ======================== 修改任务完成 ========================

    @Override
    public void completeModifyTask(String topicId) {
        TopicProcessInstance mapping = getProcessMapping(topicId);
        String processInstanceId = mapping.getProcessInstanceId();

        // 找到当前活跃的修改任务
        Task modifyTask = taskService.createTaskQuery()
                .processInstanceId(processInstanceId)
                .active()
                .list()
                .stream()
                .filter(t -> MODIFY_TASK_KEYS.contains(t.getTaskDefinitionKey()))
                .findFirst()
                .orElseThrow(() -> new BusinessException("当前课题没有待完成的修改任务，请确认课题处于需修改状态"));

        taskService.complete(modifyTask.getId());
        log.info("[Flow] 完成修改任务 - 课题ID: {}, 任务Key: {}", topicId, modifyTask.getTaskDefinitionKey());
    }

    // ======================== 任务查询 ========================

    @Override
    public List<FlowTaskVO> getMyTasks(String userId, String roleCode) {
        List<Task> tasks = new ArrayList<>();

        // 审核角色：查询候选组任务（未被签收）+ 已签收任务
        if (isReviewerRole(roleCode)) {
            // 候选组中未被签收的任务
            List<Task> candidateTasks = taskService.createTaskQuery()
                    .taskCandidateGroup(roleCode)
                    .taskUnassigned()
                    .orderByTaskCreateTime().desc()
                    .list();
            tasks.addAll(candidateTasks);

            // 当前用户已签收的任务
            List<Task> assignedTasks = taskService.createTaskQuery()
                    .taskAssignee(userId)
                    .orderByTaskCreateTime().desc()
                    .list();
            // 过滤掉修改任务（审核人不关心修改任务）
            assignedTasks.stream()
                    .filter(t -> !MODIFY_TASK_KEYS.contains(t.getTaskDefinitionKey()))
                    .forEach(tasks::add);
        } else {
            // 企业教师：查询分配给自己的修改待重提任务
            List<Task> modifyTasks = taskService.createTaskQuery()
                    .taskAssignee(userId)
                    .orderByTaskCreateTime().desc()
                    .list();
            modifyTasks.stream()
                    .filter(t -> MODIFY_TASK_KEYS.contains(t.getTaskDefinitionKey()))
                    .forEach(tasks::add);
        }

        return tasks.stream()
                .map(task -> buildFlowTaskVO(task, userId))
                .collect(Collectors.toList());
    }

    // ======================== 签收任务 ========================

    @Override
    public void claimTask(String taskId, String userId) {
        Task task = getTask(taskId);
        if (StringUtils.hasText(task.getAssignee())) {
            throw new BusinessException("该任务已被他人签收，无法重复签收");
        }
        taskService.claim(taskId, userId);
        log.info("[Flow] 签收任务 - 任务ID: {}, 签收人: {}", taskId, userId);
    }

    // ======================== 完成审查任务 ========================

    @Override
    public void completeReviewTask(String taskId, String outcome, String opinion, String userId) {
        Task task = getTask(taskId);
        String taskDefKey = task.getTaskDefinitionKey();
        String processInstanceId = task.getProcessInstanceId();

        // 若任务未签收，自动签收给当前操作人
        if (!StringUtils.hasText(task.getAssignee())) {
            taskService.claim(taskId, userId);
        } else if (!task.getAssignee().equals(userId)) {
            throw new BusinessException("该任务已由其他人签收，无权操作");
        }

        // 获取流程变量（课题ID）
        String topicId = (String) runtimeService.getVariable(processInstanceId, "topicId");
        Topic topic = topicMapper.selectById(topicId);
        if (topic == null) {
            throw new BusinessException("流程关联的课题不存在");
        }
        Integer previousStatus = topic.getReviewStatus();

        // 根据任务Key和outcome确定新状态
        Integer newReviewStatus = resolveNewReviewStatus(taskDefKey, outcome);

        // 设置完成变量（网关条件使用）
        Map<String, Object> variables = new HashMap<>();
        ReviewStage stage = TASK_STAGE_MAP.get(taskDefKey);
        if (stage == ReviewStage.PRE_REVIEW) {
            variables.put("preOutcome", outcome);
        } else if (stage == ReviewStage.INIT_REVIEW) {
            variables.put("initOutcome", outcome);
        } else {
            variables.put("finalOutcome", outcome);
        }

        // 在完成任务前获取 identity links（完成后 runtime identity links 会被清除）
        String roleCode = getRoleCodeFromTaskKey(taskDefKey);

        // 完成 Flowable 任务
        taskService.complete(taskId, variables);

        // 同步 topic.review_status
        topic.setReviewStatus(newReviewStatus);
        topicMapper.updateById(topic);

        // 流程结束时更新映射表状态
        if (newReviewStatus == TopicReviewStatus.FINAL_PASSED.getCode()
                || newReviewStatus == TopicReviewStatus.FINAL_FAILED.getCode()) {
            processInstanceMapper.update(null,
                    new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<TopicProcessInstance>()
                            .eq(TopicProcessInstance::getProcessInstanceId, processInstanceId)
                            .set(TopicProcessInstance::getProcessStatus, 1));
        }

        // 记录审查记录（与旧逻辑保持一致）
        User reviewer = userMapper.selectById(userId);
        TopicReviewRecord record = new TopicReviewRecord();
        record.setTopicId(topicId);
        record.setReviewStage(stage != null ? stage.getCode() : null);
        record.setReviewerId(userId);
        record.setReviewerRole(roleCode);
        record.setReviewerName(reviewer != null ? reviewer.getRealName() : userId);
        record.setReviewResult(mapOutcomeToResult(outcome));
        record.setReviewOpinion(opinion);
        record.setIsBatchReview(0);
        record.setPreviousStatus(previousStatus);
        record.setNewStatus(newReviewStatus);
        record.setIsModified(0);
        reviewRecordMapper.insert(record);

        log.info("[Flow] 完成审查任务 - 课题ID: {}, 任务Key: {}, 结果: {}, 新状态: {}",
                topicId, taskDefKey, outcome, newReviewStatus);
    }

    // ======================== 流程状态查询 ========================

    @Override
    public ProcessStatusVO getProcessStatus(String topicId) {
        ProcessStatusVO vo = new ProcessStatusVO();
        vo.setTopicId(topicId);

        Topic topic = topicMapper.selectById(topicId);
        if (topic != null) {
            vo.setReviewStatus(topic.getReviewStatus());
            TopicReviewStatus statusEnum = TopicReviewStatus.fromCode(topic.getReviewStatus());
            vo.setReviewStatusDesc(statusEnum != null ? statusEnum.getDesc() : "未知");
        }

        TopicProcessInstance mapping = processInstanceMapper.selectOne(
                new LambdaQueryWrapper<TopicProcessInstance>()
                        .eq(TopicProcessInstance::getTopicId, topicId)
                        .eq(TopicProcessInstance::getDeleted, 0));

        if (mapping == null) {
            vo.setProcessStarted(false);
            vo.setProcessFinished(false);
            return vo;
        }

        vo.setProcessStarted(true);
        vo.setProcessInstanceId(mapping.getProcessInstanceId());
        vo.setProcessFinished(mapping.getProcessStatus() != 0);

        // 历史流程实例信息
        HistoricProcessInstance hpi = historyService.createHistoricProcessInstanceQuery()
                .processInstanceId(mapping.getProcessInstanceId())
                .singleResult();
        if (hpi != null) {
            vo.setStartTime(toLocalDateTime(hpi.getStartTime()));
            vo.setEndTime(toLocalDateTime(hpi.getEndTime()));
        }

        // 当前活跃任务
        List<Task> activeTasks = taskService.createTaskQuery()
                .processInstanceId(mapping.getProcessInstanceId())
                .active().list();
        vo.setActiveTaskNames(activeTasks.stream().map(Task::getName).collect(Collectors.toList()));
        if (!activeTasks.isEmpty()) {
            vo.setWaitingRole(TASK_ROLE_DESC_MAP.getOrDefault(
                    activeTasks.get(0).getTaskDefinitionKey(), "未知"));
        }

        // 历史节点
        vo.setHistoryNodes(getProcessHistory(mapping.getProcessInstanceId()));
        return vo;
    }

    // ======================== 管理员监控 ========================

    @Override
    public PageResult<ProcessInstanceVO> listProcessInstances(Integer processStatus,
                                                               Integer pageNum, Integer pageSize) {
        LambdaQueryWrapper<TopicProcessInstance> wrapper = new LambdaQueryWrapper<TopicProcessInstance>()
                .eq(TopicProcessInstance::getDeleted, 0)
                .orderByDesc(TopicProcessInstance::getCreateTime);
        if (processStatus != null) {
            wrapper.eq(TopicProcessInstance::getProcessStatus, processStatus);
        }

        Page<TopicProcessInstance> page = new Page<>(pageNum, pageSize);
        Page<TopicProcessInstance> pageResult = processInstanceMapper.selectPage(page, wrapper);

        List<ProcessInstanceVO> voList = pageResult.getRecords().stream()
                .map(this::buildProcessInstanceVO)
                .collect(Collectors.toList());

        return new PageResult<>(voList, pageResult.getTotal(), pageNum.longValue(), pageSize.longValue());
    }

    // ======================== BPMN 图 ========================

    @Override
    public String getProcessDefinitionDiagramXml() {
        ProcessDefinition pd = repositoryService.createProcessDefinitionQuery()
                .processDefinitionKey(PROCESS_DEF_KEY)
                .latestVersion()
                .singleResult();
        if (pd == null) {
            throw new BusinessException("流程定义不存在，请确认已部署 topic_review.bpmn20.xml");
        }
        try (var inputStream = repositoryService.getResourceAsStream(
                pd.getDeploymentId(), pd.getResourceName())) {
            return new String(inputStream.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new BusinessException("读取流程定义文件失败：" + e.getMessage());
        }
    }

    @Override
    public String getProcessDiagramXml(String processInstanceId) {
        HistoricProcessInstance hpi = historyService.createHistoricProcessInstanceQuery()
                .processInstanceId(processInstanceId)
                .singleResult();
        if (hpi == null) {
            throw new BusinessException("流程实例不存在");
        }

        // 获取流程定义XML（BPMN源文件）
        ProcessDefinition pd = repositoryService.createProcessDefinitionQuery()
                .processDefinitionId(hpi.getProcessDefinitionId())
                .singleResult();
        if (pd == null) {
            throw new BusinessException("流程定义不存在");
        }

        try (var inputStream = repositoryService.getResourceAsStream(
                pd.getDeploymentId(), pd.getResourceName())) {
            return new String(inputStream.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new BusinessException("读取流程定义文件失败：" + e.getMessage());
        }
    }

    // ======================== 流程历史 ========================

    @Override
    public List<ProcessStatusVO.HistoryNodeVO> getProcessHistory(String processInstanceId) {
        List<HistoricActivityInstance> activities = historyService
                .createHistoricActivityInstanceQuery()
                .processInstanceId(processInstanceId)
                .orderByHistoricActivityInstanceStartTime().asc()
                .list();

        // 当前活跃的 activityId 集合
        Set<String> activeActivityIds = new HashSet<>();
        runtimeService.createExecutionQuery()
                .processInstanceId(processInstanceId)
                .list()
                .forEach(e -> {
                    if (e.getActivityId() != null) {
                        activeActivityIds.add(e.getActivityId());
                    }
                });

        return activities.stream()
                .filter(a -> "userTask".equals(a.getActivityType())
                        || "startEvent".equals(a.getActivityType())
                        || "endEvent".equals(a.getActivityType()))
                .map(a -> {
                    ProcessStatusVO.HistoryNodeVO node = new ProcessStatusVO.HistoryNodeVO();
                    node.setActivityId(a.getActivityId());
                    node.setActivityName(a.getActivityName());
                    node.setActivityType(a.getActivityType());
                    node.setAssignee(a.getAssignee());
                    node.setStartTime(toLocalDateTime(a.getStartTime()));
                    node.setEndTime(toLocalDateTime(a.getEndTime()));
                    node.setActive(activeActivityIds.contains(a.getActivityId())
                            && a.getEndTime() == null);
                    // 填充操作人姓名
                    if (StringUtils.hasText(a.getAssignee())) {
                        User u = userMapper.selectById(a.getAssignee());
                        node.setAssigneeName(u != null ? u.getRealName() : a.getAssignee());
                    }
                    return node;
                })
                .collect(Collectors.toList());
    }

    // ======================== 任务同步（来自旧审查路径） ========================

    @Override
    public void syncFlowTask(String topicId, String outcome, String reviewerId) {
        try {
            TopicProcessInstance mapping = processInstanceMapper.selectOne(
                    new LambdaQueryWrapper<TopicProcessInstance>()
                            .eq(TopicProcessInstance::getTopicId, topicId)
                            .eq(TopicProcessInstance::getDeleted, 0));
            if (mapping == null || mapping.getProcessStatus() != 0) {
                log.debug("[Flow] 课题 {} 无活跃流程实例，跳过同步", topicId);
                return;
            }

            // 找到当前活跃的审查任务（排除修改任务）
            Task activeTask = taskService.createTaskQuery()
                    .processInstanceId(mapping.getProcessInstanceId())
                    .active()
                    .list()
                    .stream()
                    .filter(t -> !MODIFY_TASK_KEYS.contains(t.getTaskDefinitionKey()))
                    .findFirst()
                    .orElse(null);

            if (activeTask == null) {
                log.debug("[Flow] 课题 {} 无活跃审查任务，跳过同步", topicId);
                return;
            }

            // 若未签收，自动签收
            if (!StringUtils.hasText(activeTask.getAssignee())) {
                taskService.claim(activeTask.getId(), reviewerId);
            }

            // 构建完成变量
            Map<String, Object> variables = new HashMap<>();
            ReviewStage stage = TASK_STAGE_MAP.get(activeTask.getTaskDefinitionKey());
            if (stage == ReviewStage.PRE_REVIEW) {
                variables.put("preOutcome", outcome);
            } else if (stage == ReviewStage.INIT_REVIEW) {
                variables.put("initOutcome", outcome);
            } else {
                variables.put("finalOutcome", outcome);
            }

            taskService.complete(activeTask.getId(), variables);

            // 若流程结束，更新映射状态
            Integer newStatus = resolveNewReviewStatus(activeTask.getTaskDefinitionKey(), outcome);
            if (newStatus == TopicReviewStatus.FINAL_PASSED.getCode()
                    || newStatus == TopicReviewStatus.FINAL_FAILED.getCode()) {
                processInstanceMapper.update(null,
                        new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<TopicProcessInstance>()
                                .eq(TopicProcessInstance::getProcessInstanceId, mapping.getProcessInstanceId())
                                .set(TopicProcessInstance::getProcessStatus, 1));
            }

            log.info("[Flow] 同步任务推进成功 - 课题ID: {}, 任务Key: {}, 结果: {}",
                    topicId, activeTask.getTaskDefinitionKey(), outcome);
        } catch (Exception e) {
            log.warn("[Flow] 同步任务异常（课题ID: {}），不影响业务审核: {}", topicId, e.getMessage());
        }
    }

    // ======================== 私有辅助方法 ========================

    private TopicProcessInstance getProcessMapping(String topicId) {
        TopicProcessInstance mapping = processInstanceMapper.selectOne(
                new LambdaQueryWrapper<TopicProcessInstance>()
                        .eq(TopicProcessInstance::getTopicId, topicId)
                        .eq(TopicProcessInstance::getDeleted, 0));
        if (mapping == null) {
            throw new BusinessException("课题尚未启动审查流程");
        }
        return mapping;
    }

    private Task getTask(String taskId) {
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        if (task == null) {
            throw new BusinessException("任务不存在或已完成");
        }
        return task;
    }

    private Integer resolveNewReviewStatus(String taskDefKey, String outcome) {
        if ("PASS".equals(outcome)) {
            Integer status = TASK_PASS_STATUS_MAP.get(taskDefKey);
            if (status == null) {
                throw new BusinessException("不支持对任务 [" + taskDefKey + "] 使用 PASS 结果");
            }
            return status;
        } else if ("NEED_MODIFY".equals(outcome)) {
            Integer status = TASK_MODIFY_STATUS_MAP.get(taskDefKey);
            if (status == null) {
                throw new BusinessException("不支持对任务 [" + taskDefKey + "] 使用 NEED_MODIFY 结果");
            }
            return status;
        } else if ("REJECT".equals(outcome)) {
            // 只有终审任务支持 REJECT
            if (!taskDefKey.startsWith("final")) {
                throw new BusinessException("只有终审任务可以使用 REJECT（终审不通过）结果");
            }
            return TopicReviewStatus.FINAL_FAILED.getCode();
        }
        throw new BusinessException("无效的审查结果: " + outcome);
    }

    /**
     * 通过任务定义Key推导操作角色代码
     * <p>
     * 不依赖 Flowable 运行时 identity links（在任务完成后 runtime links 已清除），
     * 直接从静态映射表中获取，更安全可靠。
     * </p>
     */
    private String getRoleCodeFromTaskKey(String taskDefKey) {
        ReviewStage stage = TASK_STAGE_MAP.get(taskDefKey);
        return stage != null ? stage.getRoleCode() : "";
    }

    private boolean isReviewerRole(String roleCode) {
        return "UNIVERSITY_TEACHER".equals(roleCode)
                || "MAJOR_DIRECTOR".equals(roleCode)
                || "SUPERVISOR_TEACHER".equals(roleCode)
                || "SYSTEM_ADMIN".equals(roleCode);
    }

    private FlowTaskVO buildFlowTaskVO(Task task, String currentUserId) {
        FlowTaskVO vo = new FlowTaskVO();
        vo.setTaskId(task.getId());
        vo.setTaskDefKey(task.getTaskDefinitionKey());
        vo.setTaskName(task.getName());
        vo.setProcessInstanceId(task.getProcessInstanceId());
        vo.setAssignee(task.getAssignee());
        vo.setClaimedByMe(currentUserId.equals(task.getAssignee()));
        vo.setIsModifyTask(MODIFY_TASK_KEYS.contains(task.getTaskDefinitionKey()));
        vo.setCreateTime(toLocalDateTime(task.getCreateTime()));

        // 从流程变量中获取课题信息
        Map<String, Object> vars = taskService.getVariables(task.getId());
        if (vars != null) {
            vo.setTopicId((String) vars.get("topicId"));
            vo.setTopicTitle((String) vars.get("topicTitle"));
            Object cat = vars.get("topicCategory");
            if (cat instanceof Integer catInt) {
                vo.setTopicCategory(catInt);
                TopicCategory categoryEnum = TopicCategory.fromCode(catInt);
                vo.setTopicCategoryDesc(categoryEnum != null ? categoryEnum.getDesc() : "未知");
            }
            vo.setCreatorId((String) vars.get("creatorId"));
        }

        // 候选组描述
        vo.setCandidateGroup(TASK_ROLE_DESC_MAP.getOrDefault(task.getTaskDefinitionKey(), ""));

        // 签收人姓名
        if (StringUtils.hasText(task.getAssignee())) {
            User assigneeUser = userMapper.selectById(task.getAssignee());
            vo.setAssigneeName(assigneeUser != null ? assigneeUser.getRealName() : task.getAssignee());
        }
        // 创建人姓名
        if (StringUtils.hasText(vo.getCreatorId())) {
            User creator = userMapper.selectById(vo.getCreatorId());
            vo.setCreatorName(creator != null ? creator.getRealName() : vo.getCreatorId());
        }

        return vo;
    }

    private ProcessInstanceVO buildProcessInstanceVO(TopicProcessInstance mapping) {
        ProcessInstanceVO vo = new ProcessInstanceVO();
        vo.setProcessInstanceId(mapping.getProcessInstanceId());
        vo.setTopicId(mapping.getTopicId());
        vo.setProcessStatus(mapping.getProcessStatus());
        vo.setProcessStatusDesc(mapping.getProcessStatus() == 0 ? "运行中"
                : mapping.getProcessStatus() == 1 ? "已完成" : "已终止");

        // 课题基本信息
        Topic topic = topicMapper.selectById(mapping.getTopicId());
        if (topic != null) {
            vo.setTopicTitle(topic.getTopicTitle());
            vo.setTopicCategory(topic.getTopicCategory());
            TopicCategory catEnum = TopicCategory.fromCode(topic.getTopicCategory());
            vo.setTopicCategoryDesc(catEnum != null ? catEnum.getDesc() : "未知");
            vo.setCreatorId(topic.getCreatorId());
            vo.setReviewStatus(topic.getReviewStatus());
            TopicReviewStatus statusEnum = TopicReviewStatus.fromCode(topic.getReviewStatus());
            vo.setReviewStatusDesc(statusEnum != null ? statusEnum.getDesc() : "未知");
            User creator = userMapper.selectById(topic.getCreatorId());
            vo.setCreatorName(creator != null ? creator.getRealName() : topic.getCreatorId());
        }

        // 流程历史信息
        HistoricProcessInstance hpi = historyService.createHistoricProcessInstanceQuery()
                .processInstanceId(mapping.getProcessInstanceId())
                .singleResult();
        if (hpi != null) {
            vo.setStartTime(toLocalDateTime(hpi.getStartTime()));
            vo.setEndTime(toLocalDateTime(hpi.getEndTime()));
            if (hpi.getStartTime() != null) {
                long durationMs = (hpi.getEndTime() != null ? hpi.getEndTime().getTime()
                        : System.currentTimeMillis()) - hpi.getStartTime().getTime();
                vo.setDurationDesc(formatDuration(durationMs));
            }
        }

        // 当前活跃任务（运行中）
        if (mapping.getProcessStatus() == 0) {
            List<Task> activeTasks = taskService.createTaskQuery()
                    .processInstanceId(mapping.getProcessInstanceId())
                    .active().list();
            if (!activeTasks.isEmpty()) {
                vo.setCurrentTaskName(activeTasks.get(0).getName());
                vo.setWaitingRole(TASK_ROLE_DESC_MAP.getOrDefault(
                        activeTasks.get(0).getTaskDefinitionKey(), "未知"));
            }
        }

        return vo;
    }

    private Integer mapOutcomeToResult(String outcome) {
        return switch (outcome) {
            case "PASS" -> ReviewResult.PASSED.getCode();
            case "NEED_MODIFY" -> ReviewResult.NEED_MODIFY.getCode();
            case "REJECT" -> ReviewResult.REJECTED.getCode();
            default -> ReviewResult.PASSED.getCode();
        };
    }

    private LocalDateTime toLocalDateTime(Date date) {
        if (date == null) return null;
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    }

    private String formatDuration(long ms) {
        long days = ms / (1000 * 60 * 60 * 24);
        long hours = (ms % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60);
        long minutes = (ms % (1000 * 60 * 60)) / (1000 * 60);
        if (days > 0) return days + "天" + hours + "小时";
        if (hours > 0) return hours + "小时" + minutes + "分钟";
        return minutes + "分钟";
    }
}

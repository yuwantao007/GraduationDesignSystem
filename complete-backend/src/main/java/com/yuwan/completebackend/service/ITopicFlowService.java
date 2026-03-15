package com.yuwan.completebackend.service;

import com.yuwan.completebackend.common.PageResult;
import com.yuwan.completebackend.model.vo.FlowTaskVO;
import com.yuwan.completebackend.model.vo.ProcessInstanceVO;
import com.yuwan.completebackend.model.vo.ProcessStatusVO;

import java.util.List;

/**
 * 课题审查流程服务接口
 * <p>
 * 封装 Flowable 流程引擎，提供课题审查流程的启动、任务管理、历史查询等能力。
 * </p>
 *
 * @author 系统架构师
 * @version 1.0
 * @since 2026-03-15
 */
public interface ITopicFlowService {

    /**
     * 启动课题审查流程
     * <p>
     * 在 {@code TopicServiceImpl.submitTopic()} 第一次提交（草稿→待预审）时调用。
     * 若该课题已存在流程实例（修改后重提），则由 {@link #completeModifyTask} 驱动，
     * 本方法仅用于首次提交。
     * </p>
     *
     * @param topicId       课题ID
     * @param creatorId     课题创建人（企业教师）用户ID
     * @param topicCategory 课题大类（1-高职升本 2-3+1 3-实验班）
     * @param topicTitle    课题标题
     * @return Flowable 流程实例ID
     */
    String startProcess(String topicId, String creatorId, Integer topicCategory, String topicTitle);

    /**
     * 完成修改任务（企业教师重新提交课题时调用）
     * <p>
     * 当 {@code TopicServiceImpl.submitTopic()} 检测到课题处于 PRE_MODIFY 或 INIT_MODIFY
     * 状态时，调用本方法完成当前的修改待重提任务，让流程继续路由到下一个审查节点。
     * </p>
     *
     * @param topicId 课题ID
     */
    void completeModifyTask(String topicId);

    /**
     * 查询当前用户的待办任务列表
     * <p>
     * 审核人员（高校教师、专业方向主管、督导教师）查询属于自己角色的待签收/已签收待办任务。
     * 企业教师查询属于自己的修改待重提任务。
     * </p>
     *
     * @param userId   当前用户ID
     * @param roleCode 当前用户角色代码（如 UNIVERSITY_TEACHER）
     * @return 待办任务列表
     */
    List<FlowTaskVO> getMyTasks(String userId, String roleCode);

    /**
     * 签收任务
     * <p>
     * 将候选任务（candidate task）签收到指定用户，签收后其他人不可再操作。
     * </p>
     *
     * @param taskId 任务ID
     * @param userId 签收人用户ID
     */
    void claimTask(String taskId, String userId);

    /**
     * 完成审查任务（审核人提交审核意见）
     * <p>
     * 审核人完成任务后：
     * 1. 记录审查意见到 topic_review_record（保持与旧逻辑一致的记录）
     * 2. 同步更新 topic_info.review_status
     * 3. 更新 topic_process_instance.process_status（流程结束时）
     * </p>
     *
     * @param taskId   任务ID
     * @param outcome  审查结果（PASS | NEED_MODIFY | REJECT）
     * @param opinion  审查意见文字
     * @param userId   操作人ID
     */
    void completeReviewTask(String taskId, String outcome, String opinion, String userId);

    /**
     * 获取课题的当前流程状态
     *
     * @param topicId 课题ID
     * @return 流程状态（含当前节点、历史节点、活跃任务等）
     */
    ProcessStatusVO getProcessStatus(String topicId);

    /**
     * 管理员分页查询流程实例列表
     *
     * @param processStatus 流程状态过滤（null=全部，0=运行中，1=已完成，2=已终止）
     * @param pageNum       页码（从1开始）
     * @param pageSize      每页条数
     * @return 流程实例分页结果
     */
    PageResult<ProcessInstanceVO> listProcessInstances(Integer processStatus, Integer pageNum, Integer pageSize);

    /**
     * 获取流程实例的 BPMN XML（含当前活跃节点信息，用于前端高亮）
     *
     * @param processInstanceId 流程实例ID
     * @return BPMN XML 字符串
     */
    String getProcessDiagramXml(String processInstanceId);

    /**
     * 获取课题审查流程定义的 BPMN XML（不依赖具体实例，用于流程定义可视化页面）
     * <p>
     * 从 Flowable 部署记录中查询最新版本的 topic_review 流程定义，
     * 返回 BPMN 2.0 XML 字符串供前端 bpmn-js 渲染整体流程图。
     * </p>
     *
     * @return BPMN 2.0 XML 字符串
     * @throws com.yuwan.completebackend.exception.BusinessException 流程定义未部署时抛出
     */
    String getProcessDefinitionDiagramXml();

    /**
     * 获取流程实例的完整历史节点记录
     *
     * @param processInstanceId 流程实例ID
     * @return 历史节点列表
     */
    List<ProcessStatusVO.HistoryNodeVO> getProcessHistory(String processInstanceId);

    /**
     * 通过课题ID同步推进 Flowable 任务（不重复写审查记录）
     * <p>
     * 由 {@code TopicReviewServiceImpl.reviewTopic()} 调用：审查逻辑在旧代码中完成，
     * 本方法只负责找到活跃的审查任务并根据 outcome 完成，推进流程到下一节点。
     * 若找不到活跃任务，静默跳过，不抛出异常。
     * </p>
     *
     * @param topicId    课题ID
     * @param outcome    审查结果（PASS | NEED_MODIFY | REJECT）
     * @param reviewerId 操作人ID
     */
    void syncFlowTask(String topicId, String outcome, String reviewerId);
}

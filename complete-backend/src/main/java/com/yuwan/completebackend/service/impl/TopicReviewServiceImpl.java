package com.yuwan.completebackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yuwan.completebackend.common.PageResult;
import com.yuwan.completebackend.exception.BusinessException;
import com.yuwan.completebackend.mapper.*;
import com.yuwan.completebackend.model.dto.BatchReviewDTO;
import com.yuwan.completebackend.model.dto.GeneralOpinionDTO;
import com.yuwan.completebackend.model.dto.ModifyReviewDTO;
import com.yuwan.completebackend.model.dto.ReviewTopicDTO;
import com.yuwan.completebackend.model.entity.*;
import com.yuwan.completebackend.model.enums.ReviewResult;
import com.yuwan.completebackend.model.enums.ReviewStage;
import com.yuwan.completebackend.model.enums.TopicCategory;
import com.yuwan.completebackend.model.enums.TopicReviewStatus;
import com.yuwan.completebackend.model.vo.*;
import com.yuwan.completebackend.security.SecurityUtil;
import com.yuwan.completebackend.service.INotificationDispatchService;
import com.yuwan.completebackend.service.ITopicFlowService;
import com.yuwan.completebackend.service.ITopicReviewService;
import com.yuwan.completebackend.service.ITopicService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 课题审查服务实现类
 * 提供课题审查相关的业务逻辑处理
 * 
 * <p>审查流程说明：</p>
 * <ul>
 *   <li>高职升本课题：预审(高校教师) → 初审(专业方向主管) → 终审(督导教师)</li>
 *   <li>3+1/实验班课题：初审(专业方向主管) → 终审(高校教师)</li>
 * </ul>
 * 
 * <p>关键业务规则：</p>
 * <ul>
 *   <li>企业教师最多可提交通过18个课题</li>
 *   <li>审查结果可修改，但需满足下级未通过的条件</li>
 *   <li>综合意见对本专业方向所有教师可见</li>
 * </ul>
 *
 * @author 系统架构师
 * @version 1.0
 * @since 2026-02-23
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
public class TopicReviewServiceImpl implements ITopicReviewService {

    private final TopicMapper topicMapper;
    private final TopicReviewRecordMapper reviewRecordMapper;
    private final TopicGeneralOpinionMapper generalOpinionMapper;
    private final TopicBatchReviewMapper batchReviewMapper;
    private final UserMapper userMapper;
    private final EnterpriseMapper enterpriseMapper;
    private final ITopicService topicService;
    private final ITopicFlowService topicFlowService;
    private final INotificationDispatchService notificationDispatchService;

    /**
     * 企业教师最大可通过终审的课题数量
     */
    private static final int MAX_PASSED_TOPICS = 18;

    @Override
    public PageResult<TopicReviewListVO> getPendingTopics(ReviewQueryVO queryVO) {
        // 获取当前用户角色
        String currentUserId = SecurityUtil.getCurrentUserId();
        List<String> roles = SecurityUtil.getCurrentUserRoles();

        // 构建查询条件
        LambdaQueryWrapper<Topic> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Topic::getIsSubmitted, 1);
        queryWrapper.eq(Topic::getDeleted, 0);

        // 根据角色确定查询条件
        if (roles.contains("ROLE_SYSTEM_ADMIN")) {
            // 管理员可以看到所有待审查课题
            queryWrapper.in(Topic::getReviewStatus, Arrays.asList(
                    TopicReviewStatus.PENDING_PRE.getCode(),
                    TopicReviewStatus.PRE_MODIFY.getCode(),
                    TopicReviewStatus.PRE_PASSED.getCode(),
                    TopicReviewStatus.INIT_MODIFY.getCode(),
                    TopicReviewStatus.INIT_PASSED.getCode()
            ));
        } else if (roles.contains("ROLE_UNIVERSITY_TEACHER")) {
            // 高校教师：可以预审高职升本课题 + 终审 3+1/实验班课题
            queryWrapper.and(w -> w
                    // 高职升本的待预审课题
                    .nested(n -> n
                            .eq(Topic::getTopicCategory, TopicCategory.UPGRADE.getCode())
                            .in(Topic::getReviewStatus, Arrays.asList(
                                    TopicReviewStatus.PENDING_PRE.getCode(),
                                    TopicReviewStatus.PRE_MODIFY.getCode()
                            ))
                    )
                    // 3+1/实验班的待终审课题
                    .or()
                    .nested(n -> n
                            .ne(Topic::getTopicCategory, TopicCategory.UPGRADE.getCode())
                            .eq(Topic::getReviewStatus, TopicReviewStatus.INIT_PASSED.getCode())
                    )
            );
        } else if (roles.contains("ROLE_MAJOR_DIRECTOR")) {
            // 专业方向主管：初审所有课题
            queryWrapper.and(w -> w
                    // 高职升本课题：需要预审通过
                    .nested(n -> n
                            .eq(Topic::getTopicCategory, TopicCategory.UPGRADE.getCode())
                            .in(Topic::getReviewStatus, Arrays.asList(
                                    TopicReviewStatus.PRE_PASSED.getCode(),
                                    TopicReviewStatus.INIT_MODIFY.getCode()
                            ))
                    )
                    // 3+1/实验班课题：直接初审
                    .or()
                    .nested(n -> n
                            .ne(Topic::getTopicCategory, TopicCategory.UPGRADE.getCode())
                            .in(Topic::getReviewStatus, Arrays.asList(
                                    TopicReviewStatus.PENDING_PRE.getCode(),
                                    TopicReviewStatus.INIT_MODIFY.getCode()
                            ))
                    )
            );
        } else if (roles.contains("ROLE_SUPERVISOR_TEACHER")) {
            // 督导教师：仅终审高职升本课题
            queryWrapper.eq(Topic::getTopicCategory, TopicCategory.UPGRADE.getCode());
            queryWrapper.eq(Topic::getReviewStatus, TopicReviewStatus.INIT_PASSED.getCode());
        } else {
            throw new BusinessException("当前用户没有课题审查权限");
        }

        // 添加额外查询条件
        if (StringUtils.hasText(queryVO.getTopicTitle())) {
            queryWrapper.like(Topic::getTopicTitle, queryVO.getTopicTitle());
        }
        if (queryVO.getTopicCategory() != null) {
            queryWrapper.eq(Topic::getTopicCategory, queryVO.getTopicCategory());
        }
        if (StringUtils.hasText(queryVO.getGuidanceDirection())) {
            queryWrapper.eq(Topic::getGuidanceDirection, queryVO.getGuidanceDirection());
        }
        if (StringUtils.hasText(queryVO.getCreatorId())) {
            queryWrapper.eq(Topic::getCreatorId, queryVO.getCreatorId());
        }
        if (StringUtils.hasText(queryVO.getEnterpriseId())) {
            queryWrapper.eq(Topic::getEnterpriseId, queryVO.getEnterpriseId());
        }

        queryWrapper.orderByDesc(Topic::getCreateTime);

        // 分页查询
        Page<Topic> page = new Page<>(queryVO.getPageNum(), queryVO.getPageSize());
        Page<Topic> topicPage = topicMapper.selectPage(page, queryWrapper);

        // 转换为VO
        List<TopicReviewListVO> voList = topicPage.getRecords().stream()
                .map(this::convertToReviewListVO)
                .collect(Collectors.toList());

        return new PageResult<>(voList, topicPage.getTotal());
    }

    @Override
    public TopicVO reviewTopic(ReviewTopicDTO reviewDTO) {
        // 获取当前用户信息
        String currentUserId = SecurityUtil.getCurrentUserId();
        List<String> roles = SecurityUtil.getCurrentUserRoles();
        User currentUser = userMapper.selectById(currentUserId);

        // 获取课题信息
        Topic topic = topicMapper.selectById(reviewDTO.getTopicId());
        if (topic == null) {
            throw new BusinessException("课题不存在");
        }

        // 根据课题类型和用户角色确定审查阶段
        ReviewStage stage = determineReviewStageForTopic(topic, roles);
        if (stage == null) {
            throw new BusinessException("当前用户没有该课题的审查权限");
        }

        // 验证课题状态是否可审查
        validateTopicCanBeReviewed(topic, stage, roles);

        // 验证审查结果
        ReviewResult result = ReviewResult.fromCode(reviewDTO.getReviewResult());
        if (result == null) {
            throw new BusinessException("无效的审查结果");
        }
        if (!result.isValidForStage(stage)) {
            throw new BusinessException("不通过结果仅终审可用");
        }

        // 如果是终审通过，检查企业教师的课题数量限制
        if (stage == ReviewStage.FINAL_REVIEW && result == ReviewResult.PASSED) {
            checkTeacherTopicLimit(topic.getCreatorId());
        }

        // 记录审查前状态
        Integer previousStatus = topic.getReviewStatus();

        // 更新课题状态
        TopicReviewStatus newStatus = result.toTopicStatus(stage);
        topic.setReviewStatus(newStatus.getCode());
        topicMapper.updateById(topic);

        // 创建审查记录
        TopicReviewRecord record = new TopicReviewRecord();
        record.setTopicId(topic.getTopicId());
        record.setReviewStage(stage.getCode());
        record.setReviewerId(currentUserId);
        record.setReviewerRole(getRoleCode(roles));
        record.setReviewerName(currentUser.getRealName());
        record.setReviewResult(result.getCode());
        record.setReviewOpinion(reviewDTO.getReviewOpinion());
        record.setIsBatchReview(0);
        record.setPreviousStatus(previousStatus);
        record.setNewStatus(newStatus.getCode());
        record.setIsModified(0);
        reviewRecordMapper.insert(record);

        // 同步推进 Flowable 流程（仅推进任务，不重复写审查记录）
        String outcome = switch (result) {
            case PASSED -> "PASS";
            case NEED_MODIFY -> "NEED_MODIFY";
            case REJECTED -> "REJECT";
        };
        topicFlowService.syncFlowTask(topic.getTopicId(), outcome, currentUserId);

        log.info("课题审查完成，课题ID: {}, 审查阶段: {}, 审查结果: {}, 审查人: {}",
                topic.getTopicId(), stage.getDesc(), result.getDesc(), currentUser.getRealName());

        sendReviewNotification(topic, stage, result, currentUser.getRealName(), record.getReviewId());

        return topicService.getTopicDetail(topic.getTopicId());
    }

    @Override
    public BatchReviewResultVO batchReviewTopics(BatchReviewDTO batchDTO) {
        // 获取当前用户信息
        String currentUserId = SecurityUtil.getCurrentUserId();
        List<String> roles = SecurityUtil.getCurrentUserRoles();
        User currentUser = userMapper.selectById(currentUserId);

        // 验证审查结果
        ReviewResult result = ReviewResult.fromCode(batchDTO.getReviewResult());
        if (result == null) {
            throw new BusinessException("无效的审查结果");
        }

        // 创建批次记录（先不设置 stage，后续更新）
        TopicBatchReview batchReview = new TopicBatchReview();
        batchReview.setReviewerId(currentUserId);
        batchReview.setReviewerRole(getRoleCode(roles));
        batchReview.setReviewResult(result.getCode());
        batchReview.setReviewOpinion(batchDTO.getReviewOpinion());
        batchReview.setTopicCount(batchDTO.getTopicIds().size());

        BatchReviewResultVO resultVO = new BatchReviewResultVO();
        Integer firstValidStage = null;

        // 批量处理每个课题
        for (String topicId : batchDTO.getTopicIds()) {
            try {
                Topic topic = topicMapper.selectById(topicId);
                if (topic == null) {
                    resultVO.addFailed(topicId, "未知", "课题不存在");
                    continue;
                }

                // 根据课题类型和用户角色确定审查阶段
                ReviewStage stage = determineReviewStageForTopic(topic, roles);
                if (stage == null) {
                    resultVO.addFailed(topicId, topic.getTopicTitle(), "当前用户没有该课题的审查权限");
                    continue;
                }

                // 记录第一个有效的审查阶段
                if (firstValidStage == null) {
                    firstValidStage = stage.getCode();
                }

                // 验证审查结果是否适用于该阶段
                if (!result.isValidForStage(stage)) {
                    resultVO.addFailed(topicId, topic.getTopicTitle(), "不通过结果仅终审可用");
                    continue;
                }

                // 验证课题状态
                try {
                    validateTopicCanBeReviewed(topic, stage, roles);
                } catch (BusinessException e) {
                    resultVO.addFailed(topicId, topic.getTopicTitle(), e.getMessage());
                    continue;
                }

                // 如果是终审通过，检查企业教师的课题数量限制
                if (stage == ReviewStage.FINAL_REVIEW && result == ReviewResult.PASSED) {
                    try {
                        checkTeacherTopicLimit(topic.getCreatorId());
                    } catch (BusinessException e) {
                        resultVO.addFailed(topicId, topic.getTopicTitle(), e.getMessage());
                        continue;
                    }
                }

                // 记录审查前状态
                Integer previousStatus = topic.getReviewStatus();

                // 更新课题状态
                TopicReviewStatus newStatus = result.toTopicStatus(stage);
                topic.setReviewStatus(newStatus.getCode());
                topicMapper.updateById(topic);

                // 创建审查记录
                TopicReviewRecord record = new TopicReviewRecord();
                record.setTopicId(topic.getTopicId());
                record.setReviewStage(stage.getCode());
                record.setReviewerId(currentUserId);
                record.setReviewerRole(getRoleCode(roles));
                record.setReviewerName(currentUser.getRealName());
                record.setReviewResult(result.getCode());
                record.setReviewOpinion(batchDTO.getReviewOpinion());
                record.setIsBatchReview(1);
                record.setBatchReviewId(batchReview.getBatchId());
                record.setPreviousStatus(previousStatus);
                record.setNewStatus(newStatus.getCode());
                record.setIsModified(0);
                reviewRecordMapper.insert(record);

                sendReviewNotification(topic, stage, result, currentUser.getRealName(), record.getReviewId());

                resultVO.addSuccess(topicId);

            } catch (Exception e) {
                log.error("批量审查课题异常，课题ID: {}", topicId, e);
                resultVO.addFailed(topicId, "未知", "系统异常：" + e.getMessage());
            }
        }

        // 设置批次审查阶段并保存
        batchReview.setReviewStage(firstValidStage != null ? firstValidStage : ReviewStage.INIT_REVIEW.getCode());
        batchReviewMapper.insert(batchReview);
        resultVO.setBatchId(batchReview.getBatchId());

        log.info("批量课题审查完成，批次ID: {}, 成功: {}, 失败: {}",
                batchReview.getBatchId(), resultVO.getSuccessCount(), resultVO.getFailedCount());

        return resultVO;
    }

    @Override
    public List<TopicReviewRecordVO> getReviewHistory(String topicId) {
        // 验证课题存在
        Topic topic = topicMapper.selectById(topicId);
        if (topic == null) {
            throw new BusinessException("课题不存在");
        }

        // 查询审查记录
        List<TopicReviewRecord> records = reviewRecordMapper.selectByTopicIdOrderByTime(topicId);

        // 转换为VO
        return records.stream()
                .map(this::convertToReviewRecordVO)
                .collect(Collectors.toList());
    }

    @Override
    public GeneralOpinionVO submitGeneralOpinion(GeneralOpinionDTO opinionDTO) {
        // 获取当前用户信息
        String currentUserId = SecurityUtil.getCurrentUserId();
        List<String> roles = SecurityUtil.getCurrentUserRoles();
        User currentUser = userMapper.selectById(currentUserId);

        // 验证权限：只有专业方向主管和督导教师可以提交综合意见
        if (!roles.contains("ROLE_MAJOR_DIRECTOR") && !roles.contains("ROLE_SUPERVISOR_TEACHER") 
            && !roles.contains("ROLE_SYSTEM_ADMIN")) {
            throw new BusinessException("只有专业方向主管和督导教师可以提交综合意见");
        }

        // 验证综合意见长度
        if (opinionDTO.getOpinionContent().length() > 200) {
            throw new BusinessException("综合意见不能超过200字");
        }

        // 创建综合意见
        TopicGeneralOpinion opinion = new TopicGeneralOpinion();
        opinion.setReviewerId(currentUserId);
        opinion.setReviewerRole(getRoleCode(roles));
        opinion.setReviewerName(currentUser.getRealName());
        opinion.setReviewStage(opinionDTO.getReviewStage());
        opinion.setGuidanceDirection(opinionDTO.getGuidanceDirection());
        opinion.setOpinionContent(opinionDTO.getOpinionContent());
        opinion.setTargetScope("DIRECTION");
        generalOpinionMapper.insert(opinion);

        log.info("提交综合意见，意见ID: {}, 专业方向: {}, 阶段: {}",
                opinion.getOpinionId(), opinionDTO.getGuidanceDirection(), opinionDTO.getReviewStage());

        return convertToGeneralOpinionVO(opinion);
    }

    @Override
    public List<GeneralOpinionVO> getGeneralOpinions(Integer reviewStage, String guidanceDirection) {
        List<TopicGeneralOpinion> opinions = generalOpinionMapper.selectByDirectionAndStage(
                guidanceDirection, reviewStage);

        return opinions.stream()
                .map(this::convertToGeneralOpinionVO)
                .collect(Collectors.toList());
    }

    @Override
    public TopicVO modifyReviewResult(ModifyReviewDTO modifyDTO) {
        // 获取当前用户信息
        String currentUserId = SecurityUtil.getCurrentUserId();
        User currentUser = userMapper.selectById(currentUserId);

        // 查询审查记录
        TopicReviewRecord record = reviewRecordMapper.selectById(modifyDTO.getReviewId());
        if (record == null) {
            throw new BusinessException("审查记录不存在");
        }

        // 验证是否是本人的审查记录
        if (!record.getReviewerId().equals(currentUserId)) {
            throw new BusinessException("只能修改自己的审查记录");
        }

        // 获取课题信息
        Topic topic = topicMapper.selectById(record.getTopicId());
        if (topic == null) {
            throw new BusinessException("课题不存在");
        }

        // 验证是否可以修改
        ReviewStage stage = ReviewStage.fromCode(record.getReviewStage());
        if (!canModifyReview(topic, stage)) {
            throw new BusinessException("当前课题状态不允许修改审查结果，下一阶段已通过");
        }

        // 验证新审查结果
        ReviewResult newResult = ReviewResult.fromCode(modifyDTO.getNewReviewResult());
        if (newResult == null) {
            throw new BusinessException("无效的审查结果");
        }
        if (!newResult.isValidForStage(stage)) {
            throw new BusinessException("不通过结果仅终审可用");
        }

        // 记录原状态
        Integer previousTopicStatus = topic.getReviewStatus();

        // 更新课题状态
        TopicReviewStatus newStatus = newResult.toTopicStatus(stage);
        topic.setReviewStatus(newStatus.getCode());
        topicMapper.updateById(topic);

        // 更新审查记录
        record.setReviewResult(newResult.getCode());
        if (StringUtils.hasText(modifyDTO.getNewReviewOpinion())) {
            record.setReviewOpinion(modifyDTO.getNewReviewOpinion());
        }
        record.setNewStatus(newStatus.getCode());
        record.setIsModified(1);
        record.setModifiedBy(currentUserId);
        record.setModifiedTime(new Date());
        reviewRecordMapper.updateById(record);

        log.info("修改审查结果，审查记录ID: {}, 课题ID: {}, 新结果: {}, 修改人: {}",
                record.getReviewId(), topic.getTopicId(), newResult.getDesc(), currentUser.getRealName());

        sendReviewNotification(topic, stage, newResult, currentUser.getRealName(), record.getReviewId());

        return topicService.getTopicDetail(topic.getTopicId());
    }

    private void sendReviewNotification(Topic topic,
                                        ReviewStage stage,
                                        ReviewResult result,
                                        String reviewerName,
                                        String reviewId) {
        if (topic == null || !StringUtils.hasText(topic.getCreatorId())) {
            return;
        }

        Map<String, String> variables = new HashMap<>();
        variables.put("topicTitle", topic.getTopicTitle());
        variables.put("reviewStage", stage.getDesc());
        variables.put("reviewResult", result.getDesc());
        variables.put("reviewerName", reviewerName);

        notificationDispatchService.sendByTemplateAfterCommit(
                "TOPIC_REVIEW_RESULT",
                topic.getCreatorId(),
                "TOPIC_REVIEW",
                topic.getTopicId(),
                "/topic/detail/" + topic.getTopicId(),
                variables,
                "topic:review:" + reviewId + ":" + topic.getCreatorId(),
                null,
                null
        );
    }

    @Override
    public void deleteGeneralOpinion(String opinionId) {
        // 获取当前用户信息
        String currentUserId = SecurityUtil.getCurrentUserId();

        // 查询综合意见
        TopicGeneralOpinion opinion = generalOpinionMapper.selectById(opinionId);
        if (opinion == null) {
            throw new BusinessException("综合意见不存在");
        }

        // 验证是否是本人的意见
        if (!opinion.getReviewerId().equals(currentUserId)) {
            throw new BusinessException("只能删除自己的综合意见");
        }

        // 逻辑删除
        generalOpinionMapper.deleteById(opinionId);

        log.info("删除综合意见，意见ID: {}", opinionId);
    }

    @Override
    public TeacherPassedCountVO getTeacherPassedCount(String teacherId) {
        // 查询教师信息
        User teacher = userMapper.selectById(teacherId);
        if (teacher == null) {
            throw new BusinessException("教师不存在");
        }

        // 统计通过终审的课题数
        LambdaQueryWrapper<Topic> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Topic::getCreatorId, teacherId);
        queryWrapper.eq(Topic::getReviewStatus, TopicReviewStatus.FINAL_PASSED.getCode());
        queryWrapper.eq(Topic::getDeleted, 0);
        Long count = topicMapper.selectCount(queryWrapper);

        TeacherPassedCountVO vo = new TeacherPassedCountVO();
        vo.setTeacherId(teacherId);
        vo.setTeacherName(teacher.getRealName());
        vo.setPassedCount(count.intValue());
        vo.setMaxCount(MAX_PASSED_TOPICS);
        vo.calculateRemaining();

        return vo;
    }

    @Override
    public boolean canTeacherSubmitTopic(String teacherId) {
        TeacherPassedCountVO countVO = getTeacherPassedCount(teacherId);
        return !countVO.getReachedLimit();
    }

    @Override
    public boolean canReviewTopic(String topicId) {
        try {
            List<String> roles = SecurityUtil.getCurrentUserRoles();

            Topic topic = topicMapper.selectById(topicId);
            if (topic == null) {
                return false;
            }

            // 根据课题类型和用户角色确定审查阶段
            ReviewStage stage = determineReviewStageForTopic(topic, roles);
            if (stage == null) {
                return false;
            }

            validateTopicCanBeReviewed(topic, stage, roles);
            return true;
        } catch (BusinessException e) {
            return false;
        }
    }

    @Override
    public TopicReviewRecordVO getModifiableReviewRecord(String topicId) {
        String currentUserId = SecurityUtil.getCurrentUserId();

        // 查询当前用户对该课题的审查记录
        LambdaQueryWrapper<TopicReviewRecord> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TopicReviewRecord::getTopicId, topicId);
        queryWrapper.eq(TopicReviewRecord::getReviewerId, currentUserId);
        queryWrapper.eq(TopicReviewRecord::getDeleted, 0);
        queryWrapper.orderByDesc(TopicReviewRecord::getCreateTime);
        queryWrapper.last("LIMIT 1");

        TopicReviewRecord record = reviewRecordMapper.selectOne(queryWrapper);
        if (record == null) {
            return null;
        }

        // 检查是否可修改
        Topic topic = topicMapper.selectById(topicId);
        if (topic == null) {
            return null;
        }

        ReviewStage stage = ReviewStage.fromCode(record.getReviewStage());
        if (!canModifyReview(topic, stage)) {
            return null;
        }

        return convertToReviewRecordVO(record);
    }

    // ==================== 私有方法 ====================

    /**
     * 根据用户角色确定审查阶段（默认阶段，不考虑课题类型）
     */
    private ReviewStage determineReviewStage(List<String> roles) {
        if (roles.contains("ROLE_SYSTEM_ADMIN")) {
            return ReviewStage.FINAL_REVIEW;
        }
        if (roles.contains("ROLE_UNIVERSITY_TEACHER")) {
            return ReviewStage.PRE_REVIEW;
        }
        if (roles.contains("ROLE_MAJOR_DIRECTOR")) {
            return ReviewStage.INIT_REVIEW;
        }
        if (roles.contains("ROLE_SUPERVISOR_TEACHER")) {
            return ReviewStage.FINAL_REVIEW;
        }
        return null;
    }

    /**
     * 根据课题类型和用户角色确定审查阶段
     * 
     * <p>审查流程：</p>
     * <ul>
     *   <li>高职升本：高校教师预审 → 专业方向主管初审 → 督导教师终审</li>
     *   <li>3+1/实验班：专业方向主管初审 → 高校教师终审</li>
     * </ul>
     *
     * @param topic 课题实体
     * @param roles 用户角色列表
     * @return 审查阶段，如果用户没有权限则返回null
     */
    private ReviewStage determineReviewStageForTopic(Topic topic, List<String> roles) {
        TopicCategory category = TopicCategory.fromCode(topic.getTopicCategory());
        TopicReviewStatus currentStatus = TopicReviewStatus.fromCode(topic.getReviewStatus());
        
        // 管理员可以执行任何阶段的审查
        if (roles.contains("ROLE_SYSTEM_ADMIN")) {
            // 根据当前状态判断应该执行的阶段
            if (currentStatus == TopicReviewStatus.PENDING_PRE || currentStatus == TopicReviewStatus.PRE_MODIFY) {
                return category == TopicCategory.UPGRADE ? ReviewStage.PRE_REVIEW : ReviewStage.INIT_REVIEW;
            } else if (currentStatus == TopicReviewStatus.PRE_PASSED || currentStatus == TopicReviewStatus.INIT_MODIFY) {
                return ReviewStage.INIT_REVIEW;
            } else if (currentStatus == TopicReviewStatus.INIT_PASSED) {
                return ReviewStage.FINAL_REVIEW;
            }
            return ReviewStage.FINAL_REVIEW;
        }
        
        if (category == TopicCategory.UPGRADE) {
            // 高职升本课题流程：高校教师预审 → 专业方向主管初审 → 督导教师终审
            if (roles.contains("ROLE_UNIVERSITY_TEACHER")) {
                // 高校教师只能预审高职升本课题
                if (currentStatus == TopicReviewStatus.PENDING_PRE || currentStatus == TopicReviewStatus.PRE_MODIFY) {
                    return ReviewStage.PRE_REVIEW;
                }
                return null;
            }
            if (roles.contains("ROLE_MAJOR_DIRECTOR")) {
                // 专业方向主管初审
                if (currentStatus == TopicReviewStatus.PRE_PASSED || currentStatus == TopicReviewStatus.INIT_MODIFY) {
                    return ReviewStage.INIT_REVIEW;
                }
                return null;
            }
            if (roles.contains("ROLE_SUPERVISOR_TEACHER")) {
                // 督导教师终审高职升本课题
                if (currentStatus == TopicReviewStatus.INIT_PASSED) {
                    return ReviewStage.FINAL_REVIEW;
                }
                return null;
            }
        } else {
            // 3+1/实验班课题流程：专业方向主管初审 → 高校教师终审
            if (roles.contains("ROLE_MAJOR_DIRECTOR")) {
                // 专业方向主管初审
                if (currentStatus == TopicReviewStatus.PENDING_PRE || currentStatus == TopicReviewStatus.INIT_MODIFY) {
                    return ReviewStage.INIT_REVIEW;
                }
                return null;
            }
            if (roles.contains("ROLE_UNIVERSITY_TEACHER")) {
                // 高校教师终审 3+1/实验班课题
                if (currentStatus == TopicReviewStatus.INIT_PASSED) {
                    return ReviewStage.FINAL_REVIEW;
                }
                return null;
            }
            // 督导教师不参与 3+1/实验班课题的审查
        }
        
        return null;
    }

    /**
     * 获取指定审查阶段需要查询的课题状态
     */
    private List<Integer> getTargetStatusesForStage(ReviewStage stage) {
        return switch (stage) {
            case PRE_REVIEW -> Arrays.asList(
                    TopicReviewStatus.PENDING_PRE.getCode(),
                    TopicReviewStatus.PRE_MODIFY.getCode()
            );
            case INIT_REVIEW -> Arrays.asList(
                    TopicReviewStatus.PRE_PASSED.getCode(),
                    TopicReviewStatus.PENDING_PRE.getCode(), // 3+1/实验班课题直接进入初审
                    TopicReviewStatus.INIT_MODIFY.getCode()
            );
            case FINAL_REVIEW -> Arrays.asList(
                    TopicReviewStatus.INIT_PASSED.getCode()
            );
        };
    }

    /**
     * 验证课题是否可以被审查
     * 
     * @param topic 课题实体
     * @param stage 审查阶段
     * @param roles 用户角色列表
     */
    private void validateTopicCanBeReviewed(Topic topic, ReviewStage stage, List<String> roles) {
        TopicReviewStatus currentStatus = TopicReviewStatus.fromCode(topic.getReviewStatus());
        TopicCategory category = TopicCategory.fromCode(topic.getTopicCategory());

        switch (stage) {
            case PRE_REVIEW -> {
                // 高校教师预审：只能审查待预审或预审需修改的高职升本课题
                if (category != TopicCategory.UPGRADE) {
                    throw new BusinessException("3+1/实验班课题无需预审，请等待初审");
                }
                if (currentStatus != TopicReviewStatus.PENDING_PRE && currentStatus != TopicReviewStatus.PRE_MODIFY) {
                    throw new BusinessException("课题当前状态不允许预审");
                }
            }
            case INIT_REVIEW -> {
                // 专业方向主管初审
                if (category == TopicCategory.UPGRADE) {
                    // 高职升本课题：需要预审通过
                    if (currentStatus != TopicReviewStatus.PRE_PASSED && currentStatus != TopicReviewStatus.INIT_MODIFY) {
                        throw new BusinessException("高职升本课题需要先通过预审");
                    }
                } else {
                    // 3+1/实验班课题：直接初审
                    if (currentStatus != TopicReviewStatus.PENDING_PRE && currentStatus != TopicReviewStatus.INIT_MODIFY) {
                        throw new BusinessException("课题当前状态不允许初审");
                    }
                }
            }
            case FINAL_REVIEW -> {
                // 终审：需要初审通过
                if (currentStatus != TopicReviewStatus.INIT_PASSED) {
                    throw new BusinessException("课题需要先通过初审");
                }
                // 验证终审角色权限
                if (category == TopicCategory.UPGRADE) {
                    // 高职升本课题：只有督导教师可以终审
                    if (!roles.contains("ROLE_SUPERVISOR_TEACHER") && !roles.contains("ROLE_SYSTEM_ADMIN")) {
                        throw new BusinessException("高职升本课题的终审需要督导教师执行");
                    }
                } else {
                    // 3+1/实验班课题：只有高校教师可以终审
                    if (!roles.contains("ROLE_UNIVERSITY_TEACHER") && !roles.contains("ROLE_SYSTEM_ADMIN")) {
                        throw new BusinessException("3+1/实验班课题的终审需要高校教师执行");
                    }
                }
            }
        }
    }

    /**
     * 检查企业教师的课题数量限制
     */
    private void checkTeacherTopicLimit(String teacherId) {
        TeacherPassedCountVO countVO = getTeacherPassedCount(teacherId);
        if (countVO.getReachedLimit()) {
            User teacher = userMapper.selectById(teacherId);
            String teacherName = teacher != null ? teacher.getRealName() : teacherId;
            throw new BusinessException(String.format(
                    "企业教师[%s]已通过终审的课题数量已达上限(%d个)，无法继续通过新课题",
                    teacherName, MAX_PASSED_TOPICS));
        }
    }

    /**
     * 判断审查结果是否可以修改
     * 规则：只有下一阶段未通过时才能修改
     */
    private boolean canModifyReview(Topic topic, ReviewStage stage) {
        TopicReviewStatus currentStatus = TopicReviewStatus.fromCode(topic.getReviewStatus());
        return stage.canModifyReview(currentStatus);
    }

    /**
     * 从角色列表中获取主要角色代码
     */
    private String getRoleCode(List<String> roles) {
        // 按优先级返回角色
        if (roles.contains("ROLE_SUPERVISOR_TEACHER")) {
            return "SUPERVISOR_TEACHER";
        }
        if (roles.contains("ROLE_MAJOR_DIRECTOR")) {
            return "MAJOR_DIRECTOR";
        }
        if (roles.contains("ROLE_UNIVERSITY_TEACHER")) {
            return "UNIVERSITY_TEACHER";
        }
        if (roles.contains("ROLE_SYSTEM_ADMIN")) {
            return "SYSTEM_ADMIN";
        }
        return roles.isEmpty() ? "UNKNOWN" : roles.get(0).replace("ROLE_", "");
    }

    /**
     * 转换为待审查课题列表VO
     */
    private TopicReviewListVO convertToReviewListVO(Topic topic) {
        TopicReviewListVO vo = new TopicReviewListVO();
        BeanUtils.copyProperties(topic, vo);

        // 设置大类名称
        TopicCategory category = TopicCategory.fromCode(topic.getTopicCategory());
        if (category != null) {
            vo.setTopicCategoryName(category.getDesc());
        }

        // 设置类型名称
        if (topic.getTopicType() != null) {
            vo.setTopicTypeName(topic.getTopicType() == 1 ? "设计" : "论文");
        }

        // 设置状态名称
        TopicReviewStatus status = TopicReviewStatus.fromCode(topic.getReviewStatus());
        if (status != null) {
            vo.setReviewStatusName(status.getDesc());
        }

        // 获取企业名称
        if (StringUtils.hasText(topic.getEnterpriseId())) {
            Enterprise enterprise = enterpriseMapper.selectById(topic.getEnterpriseId());
            if (enterprise != null) {
                vo.setEnterpriseName(enterprise.getEnterpriseName());
            }
        }

        // 获取创建人名称
        if (StringUtils.hasText(topic.getCreatorId())) {
            User creator = userMapper.selectById(topic.getCreatorId());
            if (creator != null) {
                vo.setCreatorName(creator.getRealName());
            }
        }

        // 设置是否可审批
        vo.setCanReview(canReviewTopic(topic.getTopicId()));

        // 获取审查记录数
        Integer reviewCount = reviewRecordMapper.countByTopicId(topic.getTopicId());
        vo.setReviewCount(reviewCount);

        // 获取最近审查意见
        List<TopicReviewRecord> records = reviewRecordMapper.selectByTopicIdOrderByTime(topic.getTopicId());
        if (!records.isEmpty()) {
            TopicReviewRecord latestRecord = records.get(0);
            vo.setLastReviewTime(latestRecord.getCreateTime());
            String opinion = latestRecord.getReviewOpinion();
            if (StringUtils.hasText(opinion)) {
                vo.setLastReviewOpinion(opinion.length() > 50 ? opinion.substring(0, 50) + "..." : opinion);
            }
        }

        return vo;
    }

    /**
     * 转换为审查记录VO
     */
    private TopicReviewRecordVO convertToReviewRecordVO(TopicReviewRecord record) {
        TopicReviewRecordVO vo = new TopicReviewRecordVO();
        BeanUtils.copyProperties(record, vo);

        // 设置阶段名称
        ReviewStage stage = ReviewStage.fromCode(record.getReviewStage());
        if (stage != null) {
            vo.setReviewStageName(stage.getDesc());
        }

        // 设置结果名称
        ReviewResult result = ReviewResult.fromCode(record.getReviewResult());
        if (result != null) {
            vo.setReviewResultName(result.getDesc());
        }

        // 设置角色名称
        vo.setReviewerRoleName(getRoleName(record.getReviewerRole()));

        // 设置是否批量审查
        vo.setIsBatchReview(record.getIsBatchReview() != null && record.getIsBatchReview() == 1);

        // 设置是否被修改
        vo.setIsModified(record.getIsModified() != null && record.getIsModified() == 1);

        // 获取修改人姓名
        if (vo.getIsModified() && StringUtils.hasText(record.getModifiedBy())) {
            User modifier = userMapper.selectById(record.getModifiedBy());
            if (modifier != null) {
                vo.setModifiedByName(modifier.getRealName());
            }
        }

        // 获取课题名称
        Topic topic = topicMapper.selectById(record.getTopicId());
        if (topic != null) {
            vo.setTopicTitle(topic.getTopicTitle());
        }

        return vo;
    }

    /**
     * 转换为综合意见VO
     */
    private GeneralOpinionVO convertToGeneralOpinionVO(TopicGeneralOpinion opinion) {
        GeneralOpinionVO vo = new GeneralOpinionVO();
        BeanUtils.copyProperties(opinion, vo);

        // 设置阶段名称
        ReviewStage stage = ReviewStage.fromCode(opinion.getReviewStage());
        if (stage != null) {
            vo.setReviewStageName(stage.getDesc());
        }

        // 设置角色名称
        vo.setReviewerRoleName(getRoleName(opinion.getReviewerRole()));

        return vo;
    }

    /**
     * 获取角色中文名称
     */
    private String getRoleName(String roleCode) {
        return switch (roleCode) {
            case "UNIVERSITY_TEACHER" -> "高校教师";
            case "MAJOR_DIRECTOR" -> "专业方向主管";
            case "SUPERVISOR_TEACHER" -> "督导教师";
            case "SYSTEM_ADMIN" -> "系统管理员";
            default -> roleCode;
        };
    }
}

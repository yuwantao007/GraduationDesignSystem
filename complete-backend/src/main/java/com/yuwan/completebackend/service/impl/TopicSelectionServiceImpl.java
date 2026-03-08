package com.yuwan.completebackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yuwan.completebackend.common.PageResult;
import com.yuwan.completebackend.exception.BusinessException;
import com.yuwan.completebackend.mapper.TopicMapper;
import com.yuwan.completebackend.mapper.TopicSelectionMapper;
import com.yuwan.completebackend.mapper.UserMapper;
import com.yuwan.completebackend.model.entity.Topic;
import com.yuwan.completebackend.model.entity.TopicSelection;
import com.yuwan.completebackend.model.entity.User;
import com.yuwan.completebackend.model.dto.ApplyTopicDTO;
import com.yuwan.completebackend.model.enums.TopicReviewStatus;
import com.yuwan.completebackend.model.vo.TopicForSelectionVO;
import com.yuwan.completebackend.model.vo.TopicSelectionVO;
import com.yuwan.completebackend.security.SecurityUtil;
import com.yuwan.completebackend.service.ITopicSelectionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

/**
 * 课题选报服务实现类
 *
 * @author 系统架构师
 * @version 1.0
 * @since 2026-03-08
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TopicSelectionServiceImpl implements ITopicSelectionService {

    private final TopicSelectionMapper topicSelectionMapper;
    private final TopicMapper topicMapper;
    private final UserMapper userMapper;

    /** 学生最多选报课题数 */
    private static final int MAX_SELECTION_COUNT = 3;

    @Override
    public PageResult<TopicForSelectionVO> getAvailableTopics(
            Integer topicCategory,
            String guidanceDirection,
            String topicTitle,
            String majorId,
            int pageNum,
            int pageSize
    ) {
        String studentId = SecurityUtil.getCurrentUserId();
        if (studentId == null) {
            throw new BusinessException("用户未登录");
        }

        // 查询全部可选课题
        List<TopicForSelectionVO> allTopics = topicSelectionMapper.selectAvailableTopics(
                majorId, topicCategory, guidanceDirection, topicTitle, studentId
        );

        // 手动分页
        long total = allTopics.size();
        int fromIndex = (pageNum - 1) * pageSize;
        if (fromIndex >= total) {
            return new PageResult<>(List.of(), total, (long) pageNum, (long) pageSize);
        }
        int toIndex = Math.min(fromIndex + pageSize, (int) total);
        List<TopicForSelectionVO> records = allTopics.subList(fromIndex, toIndex);

        return new PageResult<>(records, total, (long) pageNum, (long) pageSize);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TopicSelectionVO applyTopic(ApplyTopicDTO dto) {
        String studentId = SecurityUtil.getCurrentUserId();
        if (studentId == null) {
            throw new BusinessException("用户未登录");
        }

        // 1. 校验学生手机号是否已完善
        User student = userMapper.selectById(studentId);
        if (student == null) {
            throw new BusinessException("用户信息不存在");
        }
        if (student.getUserPhone() == null || student.getUserPhone().isBlank()) {
            throw new BusinessException("请先完善个人信息（手机号必填），再进行课题选报");
        }

        // 2. 校验是否已有中选记录
        long selectedCount = topicSelectionMapper.selectCount(
                new LambdaQueryWrapper<TopicSelection>()
                        .eq(TopicSelection::getStudentId, studentId)
                        .eq(TopicSelection::getSelectionStatus, 1)
        );
        if (selectedCount > 0) {
            throw new BusinessException("您已中选课题，无需继续选报");
        }

        // 3. 校验选报数量上限
        long activeCount = topicSelectionMapper.selectCount(
                new LambdaQueryWrapper<TopicSelection>()
                        .eq(TopicSelection::getStudentId, studentId)
                        .in(TopicSelection::getSelectionStatus, 0, 1)
        );
        if (activeCount >= MAX_SELECTION_COUNT) {
            throw new BusinessException("选报数量已达上限（最多" + MAX_SELECTION_COUNT + "个）");
        }

        // 4. 校验是否已选报该课题
        long existCount = topicSelectionMapper.selectCount(
                new LambdaQueryWrapper<TopicSelection>()
                        .eq(TopicSelection::getStudentId, studentId)
                        .eq(TopicSelection::getTopicId, dto.getTopicId())
                        .in(TopicSelection::getSelectionStatus, 0, 1)
        );
        if (existCount > 0) {
            throw new BusinessException("您已选报过该课题，不能重复选报");
        }

        // 5. 校验课题状态（必须终审通过）
        Topic topic = topicMapper.selectById(dto.getTopicId());
        if (topic == null || topic.getDeleted() != null && topic.getDeleted() == 1) {
            throw new BusinessException("课题不存在");
        }
        if (topic.getReviewStatus() != TopicReviewStatus.FINAL_PASSED.getCode()) {
            throw new BusinessException("该课题尚未通过终审，无法选报");
        }

        // 6. 创建选报记录
        TopicSelection selection = new TopicSelection();
        selection.setStudentId(studentId);
        selection.setTopicId(dto.getTopicId());
        selection.setSelectionReason(dto.getSelectionReason());
        selection.setSelectionStatus(0);
        selection.setApplyTime(new Date());
        topicSelectionMapper.insert(selection);

        log.info("学生 {} 选报课题 {} 成功，选报ID: {}", studentId, dto.getTopicId(), selection.getSelectionId());

        // 7. 查询并返回选报记录详情
        List<TopicSelectionVO> mySelections = topicSelectionMapper.selectMySelections(studentId);
        return mySelections.stream()
                .filter(s -> s.getSelectionId().equals(selection.getSelectionId()))
                .findFirst()
                .orElseThrow(() -> new BusinessException("选报记录创建异常"));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteSelection(String selectionId) {
        String studentId = SecurityUtil.getCurrentUserId();
        if (studentId == null) {
            throw new BusinessException("用户未登录");
        }

        // 1. 查询选报记录
        TopicSelection selection = topicSelectionMapper.selectById(selectionId);
        if (selection == null) {
            throw new BusinessException("选报记录不存在");
        }

        // 2. 校验归属（防止越权删除他人记录）
        if (!selection.getStudentId().equals(studentId)) {
            throw new BusinessException("无权操作该选报记录");
        }

        // 3. 仅落选状态可删除
        if (selection.getSelectionStatus() != 2) {
            throw new BusinessException("仅落选的选报记录可以删除");
        }

        // 4. 逻辑删除
        topicSelectionMapper.deleteById(selectionId);
        log.info("学生 {} 删除选报记录 {}", studentId, selectionId);
    }

    @Override
    public List<TopicSelectionVO> getMySelections() {
        String studentId = SecurityUtil.getCurrentUserId();
        if (studentId == null) {
            throw new BusinessException("用户未登录");
        }
        return topicSelectionMapper.selectMySelections(studentId);
    }
}

package com.yuwan.completebackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yuwan.completebackend.common.PageResult;
import com.yuwan.completebackend.exception.BusinessException;
import com.yuwan.completebackend.mapper.EnterpriseMapper;
import com.yuwan.completebackend.mapper.TopicMapper;
import com.yuwan.completebackend.mapper.UserMapper;
import com.yuwan.completebackend.model.dto.CreateTopicDTO;
import com.yuwan.completebackend.model.dto.SubmitTopicDTO;
import com.yuwan.completebackend.model.dto.TopicSignDTO;
import com.yuwan.completebackend.model.dto.UpdateTopicDTO;
import com.yuwan.completebackend.model.entity.Enterprise;
import com.yuwan.completebackend.model.entity.Topic;
import com.yuwan.completebackend.model.entity.User;
import com.yuwan.completebackend.model.vo.TopicListVO;
import com.yuwan.completebackend.model.vo.TopicQueryVO;
import com.yuwan.completebackend.model.enums.TopicCategory;
import com.yuwan.completebackend.model.enums.TopicReviewStatus;
import com.yuwan.completebackend.model.enums.TopicSignType;
import com.yuwan.completebackend.model.enums.TopicSource;
import com.yuwan.completebackend.model.enums.TopicType;
import com.yuwan.completebackend.model.vo.TopicVO;
import com.yuwan.completebackend.security.SecurityUtil;
import com.yuwan.completebackend.service.ITopicService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 课题管理服务实现类
 * 提供课题申报CRUD、提交、签名等功能实现
 *
 * @author 系统架构师
 * @version 1.0
 * @since 2026-02-21
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
public class TopicServiceImpl implements ITopicService {

    private final TopicMapper topicMapper;
    private final EnterpriseMapper enterpriseMapper;
    private final UserMapper userMapper;

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static final SimpleDateFormat DATE_ONLY_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

    @Override
    public TopicVO createTopic(CreateTopicDTO createDTO) {
        // 获取当前用户
        String currentUserId = SecurityUtil.getCurrentUserId();

        // 验证课题名称唯一性
        if (isTopicTitleExists(createDTO.getTopicTitle(), null)) {
            throw new BusinessException("课题名称已存在");
        }

        // 验证企业是否存在
        Enterprise enterprise = enterpriseMapper.selectById(createDTO.getEnterpriseId());
        if (enterprise == null) {
            throw new BusinessException("归属企业不存在");
        }

        // 验证3+1/实验班必须填写适用学校
        TopicCategory category = TopicCategory.fromCode(createDTO.getTopicCategory());
        if (category != null && category.requiresApplicableSchool() 
                && !StringUtils.hasText(createDTO.getApplicableSchool())) {
            throw new BusinessException("3+1/实验班课题必须填写适用学校");
        }

        // 创建课题实体
        Topic topic = new Topic();
        BeanUtils.copyProperties(createDTO, topic);
        topic.setCreatorId(currentUserId);
        topic.setReviewStatus(TopicReviewStatus.DRAFT.getCode());
        topic.setIsSubmitted(0);

        // 保存到数据库
        topicMapper.insert(topic);
        log.info("创建课题成功，课题ID: {}, 创建人: {}", topic.getTopicId(), currentUserId);

        return getTopicDetail(topic.getTopicId());
    }

    @Override
    public TopicVO updateTopic(String topicId, UpdateTopicDTO updateDTO) {
        // 查询课题是否存在
        Topic topic = topicMapper.selectById(topicId);
        if (topic == null) {
            throw new BusinessException("课题不存在");
        }

        // 获取当前用户
        String currentUserId = SecurityUtil.getCurrentUserId();

        // 验证是否为创建人
        if (!topic.getCreatorId().equals(currentUserId)) {
            throw new BusinessException("无权限修改此课题");
        }

        // 验证课题状态（预审通过后不可删除，但可修改内容）
        TopicReviewStatus currentStatus = TopicReviewStatus.fromCode(topic.getReviewStatus());
        if (currentStatus != null && currentStatus.isAfterPrePassed()) {
            // 预审通过后仅可修改内容，不可修改基本分类信息
            log.info("课题已通过预审，仅可修改内容信息");
        }

        // 验证课题名称唯一性
        if (StringUtils.hasText(updateDTO.getTopicTitle()) 
                && !updateDTO.getTopicTitle().equals(topic.getTopicTitle())) {
            if (isTopicTitleExists(updateDTO.getTopicTitle(), topicId)) {
                throw new BusinessException("课题名称已存在");
            }
            topic.setTopicTitle(updateDTO.getTopicTitle());
        }

        // 验证3+1/实验班必须填写适用学校
        Integer newCategoryCode = updateDTO.getTopicCategory() != null 
                ? updateDTO.getTopicCategory() : topic.getTopicCategory();
        TopicCategory newCategory = TopicCategory.fromCode(newCategoryCode);
        if (newCategory != null && newCategory.requiresApplicableSchool()) {
            String newSchool = StringUtils.hasText(updateDTO.getApplicableSchool()) 
                    ? updateDTO.getApplicableSchool() : topic.getApplicableSchool();
            if (!StringUtils.hasText(newSchool)) {
                throw new BusinessException("3+1/实验班课题必须填写适用学校");
            }
        }

        // 更新字段
        if (updateDTO.getTopicCategory() != null) {
            topic.setTopicCategory(updateDTO.getTopicCategory());
        }
        if (updateDTO.getTopicType() != null) {
            topic.setTopicType(updateDTO.getTopicType());
        }
        if (updateDTO.getTopicSource() != null) {
            topic.setTopicSource(updateDTO.getTopicSource());
        }
        if (StringUtils.hasText(updateDTO.getApplicableSchool())) {
            topic.setApplicableSchool(updateDTO.getApplicableSchool());
        }
        if (StringUtils.hasText(updateDTO.getGuidanceDirection())) {
            topic.setGuidanceDirection(updateDTO.getGuidanceDirection());
        }
        if (StringUtils.hasText(updateDTO.getBackgroundSignificance())) {
            topic.setBackgroundSignificance(updateDTO.getBackgroundSignificance());
        }
        if (StringUtils.hasText(updateDTO.getContentSummary())) {
            topic.setContentSummary(updateDTO.getContentSummary());
        }
        if (StringUtils.hasText(updateDTO.getProfessionalTraining())) {
            topic.setProfessionalTraining(updateDTO.getProfessionalTraining());
        }
        if (updateDTO.getDevelopmentEnvironment() != null) {
            topic.setDevelopmentEnvironment(updateDTO.getDevelopmentEnvironment());
        }
        if (updateDTO.getWorkloadWeeks() != null) {
            topic.setWorkloadWeeks(updateDTO.getWorkloadWeeks());
        }
        if (updateDTO.getWorkloadDetail() != null) {
            topic.setWorkloadDetail(updateDTO.getWorkloadDetail());
        }
        if (updateDTO.getScheduleRequirements() != null) {
            topic.setScheduleRequirements(updateDTO.getScheduleRequirements());
        }
        if (updateDTO.getTopicReferences() != null) {
            topic.setTopicReferences(updateDTO.getTopicReferences());
        }
        if (updateDTO.getStartDate() != null) {
            topic.setStartDate(updateDTO.getStartDate());
        }
        if (updateDTO.getEndDate() != null) {
            topic.setEndDate(updateDTO.getEndDate());
        }
        if (StringUtils.hasText(updateDTO.getRemark())) {
            topic.setRemark(updateDTO.getRemark());
        }

        topicMapper.updateById(topic);
        log.info("更新课题成功，课题ID: {}", topicId);

        return getTopicDetail(topicId);
    }

    @Override
    public TopicVO getTopicDetail(String topicId) {
        Topic topic = topicMapper.selectById(topicId);
        if (topic == null) {
            throw new BusinessException("课题不存在");
        }
        return convertToVO(topic);
    }

    @Override
    public PageResult<TopicListVO> getTopicList(TopicQueryVO queryVO) {
        Page<TopicListVO> page = new Page<>(queryVO.getPageNum(), queryVO.getPageSize());
        Page<TopicListVO> result = (Page<TopicListVO>) topicMapper.selectTopicListPage(page, queryVO);
        
        return new PageResult<>(result.getRecords(), result.getTotal(), 
                queryVO.getPageNum().longValue(), queryVO.getPageSize().longValue());
    }

    @Override
    public void deleteTopic(String topicId) {
        Topic topic = topicMapper.selectById(topicId);
        if (topic == null) {
            throw new BusinessException("课题不存在");
        }

        // 获取当前用户
        String currentUserId = SecurityUtil.getCurrentUserId();

        // 验证是否为创建人
        if (!topic.getCreatorId().equals(currentUserId)) {
            throw new BusinessException("无权限删除此课题");
        }

        // 验证课题状态（预审通过后不可删除）
        TopicReviewStatus status = TopicReviewStatus.fromCode(topic.getReviewStatus());
        if (status != null && !status.isDeletable()) {
            throw new BusinessException("课题已通过预审，不可删除");
        }

        topicMapper.deleteById(topicId);
        log.info("删除课题成功，课题ID: {}", topicId);
    }

    @Override
    public TopicVO submitTopic(SubmitTopicDTO submitDTO) {
        Topic topic = topicMapper.selectById(submitDTO.getTopicId());
        if (topic == null) {
            throw new BusinessException("课题不存在");
        }

        // 获取当前用户
        String currentUserId = SecurityUtil.getCurrentUserId();

        // 验证是否为创建人
        if (!topic.getCreatorId().equals(currentUserId)) {
            throw new BusinessException("无权限提交此课题");
        }

        // 验证课题状态（仅草稿或需修改状态可提交）
        TopicReviewStatus status = TopicReviewStatus.fromCode(topic.getReviewStatus());
        if (status == null || !status.isSubmittable()) {
            throw new BusinessException("当前状态不可提交");
        }

        // 验证必填字段
        validateTopicForSubmit(topic);

        // 更新状态为待预审
        topic.setReviewStatus(TopicReviewStatus.PENDING_PRE.getCode());
        topic.setIsSubmitted(1);
        topicMapper.updateById(topic);

        log.info("提交课题成功，课题ID: {}", submitDTO.getTopicId());
        return getTopicDetail(submitDTO.getTopicId());
    }

    @Override
    public TopicVO withdrawTopic(String topicId) {
        Topic topic = topicMapper.selectById(topicId);
        if (topic == null) {
            throw new BusinessException("课题不存在");
        }

        // 获取当前用户
        String currentUserId = SecurityUtil.getCurrentUserId();

        // 验证是否为创建人
        if (!topic.getCreatorId().equals(currentUserId)) {
            throw new BusinessException("无权限撤回此课题");
        }

        // 验证课题状态（仅待预审状态可撤回）
        TopicReviewStatus status = TopicReviewStatus.fromCode(topic.getReviewStatus());
        if (status == null || !status.isWithdrawable()) {
            throw new BusinessException("当前状态不可撤回");
        }

        // 更新状态为草稿
        topic.setReviewStatus(TopicReviewStatus.DRAFT.getCode());
        topic.setIsSubmitted(0);
        topicMapper.updateById(topic);

        log.info("撤回课题成功，课题ID: {}", topicId);
        return getTopicDetail(topicId);
    }

    @Override
    public TopicVO signTopic(TopicSignDTO signDTO) {
        Topic topic = topicMapper.selectById(signDTO.getTopicId());
        if (topic == null) {
            throw new BusinessException("课题不存在");
        }

        Date now = new Date();
        TopicSignType signType = TopicSignType.fromCode(signDTO.getSignType());
        
        if (signType == null) {
            throw new BusinessException("签名类型无效");
        }

        // 根据签名类型设置不同的签名字段
        switch (signType) {
            case COLLEGE_LEADER:
                topic.setCollegeLeaderSign(signDTO.getSignImage());
                topic.setCollegeLeaderSignTime(now);
                break;
            case ENTERPRISE_LEADER:
                topic.setEnterpriseLeaderSign(signDTO.getSignImage());
                topic.setEnterpriseLeaderSignTime(now);
                break;
            case ENTERPRISE_TEACHER:
                topic.setEnterpriseTeacherSign(signDTO.getSignImage());
                topic.setEnterpriseTeacherSignTime(now);
                break;
        }

        topicMapper.updateById(topic);
        log.info("课题签名成功，课题ID: {}, 签名类型: {}", signDTO.getTopicId(), signType.getDesc());

        return getTopicDetail(signDTO.getTopicId());
    }

    @Override
    public PageResult<TopicListVO> getMyTopics(TopicQueryVO queryVO) {
        // 设置当前用户为创建人
        String currentUserId = SecurityUtil.getCurrentUserId();
        queryVO.setCreatorId(currentUserId);
        
        return getTopicList(queryVO);
    }

    @Override
    public boolean isTopicTitleExists(String topicTitle, String excludeId) {
        return topicMapper.countByTopicTitle(topicTitle, excludeId) > 0;
    }

    @Override
    public int countPassedTopics(String creatorId) {
        return topicMapper.countPassedTopicsByCreator(creatorId);
    }

    /**
     * 验证课题提交前的必填字段
     *
     * @param topic 课题实体
     */
    private void validateTopicForSubmit(Topic topic) {
        if (!StringUtils.hasText(topic.getTopicTitle())) {
            throw new BusinessException("课题名称不能为空");
        }
        if (topic.getTopicCategory() == null) {
            throw new BusinessException("课题大类不能为空");
        }
        if (topic.getTopicType() == null) {
            throw new BusinessException("课题类型不能为空");
        }
        if (topic.getTopicSource() == null) {
            throw new BusinessException("课题来源不能为空");
        }
        if (!StringUtils.hasText(topic.getBackgroundSignificance())) {
            throw new BusinessException("选题背景与意义不能为空");
        }
        if (topic.getBackgroundSignificance().length() < 150) {
            throw new BusinessException("选题背景与意义不少于150字");
        }
        if (!StringUtils.hasText(topic.getContentSummary())) {
            throw new BusinessException("课题内容简述不能为空");
        }
        if (topic.getContentSummary().length() < 150) {
            throw new BusinessException("课题内容简述不少于150字");
        }
        if (!StringUtils.hasText(topic.getProfessionalTraining())) {
            throw new BusinessException("专业知识综合训练情况不能为空");
        }
        if (topic.getProfessionalTraining().length() < 100) {
            throw new BusinessException("专业知识综合训练情况不少于100字");
        }
        // 验证3+1/实验班必须填写适用学校
        TopicCategory category = TopicCategory.fromCode(topic.getTopicCategory());
        if (category != null && category.requiresApplicableSchool() 
                && !StringUtils.hasText(topic.getApplicableSchool())) {
            throw new BusinessException("3+1/实验班课题必须填写适用学校");
        }
    }

    /**
     * 将课题实体转换为VO
     *
     * @param topic 课题实体
     * @return 课题VO
     */
    private TopicVO convertToVO(Topic topic) {
        TopicVO vo = new TopicVO();
        BeanUtils.copyProperties(topic, vo);

        // 设置描述字段（使用枚举获取描述）
        TopicCategory category = TopicCategory.fromCode(topic.getTopicCategory());
        vo.setTopicCategoryDesc(category != null ? category.getDesc() : "未知");
        
        TopicType type = TopicType.fromCode(topic.getTopicType());
        vo.setTopicTypeDesc(type != null ? type.getDesc() : "未知");
        
        TopicSource source = TopicSource.fromCode(topic.getTopicSource());
        vo.setTopicSourceDesc(source != null ? source.getDesc() : "未知");
        
        TopicReviewStatus status = TopicReviewStatus.fromCode(topic.getReviewStatus());
        vo.setReviewStatusDesc(status != null ? status.getDesc() : "未知");

        // 查询企业名称
        Enterprise enterprise = enterpriseMapper.selectById(topic.getEnterpriseId());
        if (enterprise != null) {
            vo.setEnterpriseName(enterprise.getEnterpriseName());
        }

        // 查询创建人姓名
        User creator = userMapper.selectById(topic.getCreatorId());
        if (creator != null) {
            vo.setCreatorName(creator.getRealName());
        }

        // 格式化时间
        if (topic.getStartDate() != null) {
            vo.setStartDate(DATE_ONLY_FORMAT.format(topic.getStartDate()));
        }
        if (topic.getEndDate() != null) {
            vo.setEndDate(DATE_ONLY_FORMAT.format(topic.getEndDate()));
        }
        if (topic.getCollegeLeaderSignTime() != null) {
            vo.setCollegeLeaderSignTime(DATE_FORMAT.format(topic.getCollegeLeaderSignTime()));
        }
        if (topic.getEnterpriseLeaderSignTime() != null) {
            vo.setEnterpriseLeaderSignTime(DATE_FORMAT.format(topic.getEnterpriseLeaderSignTime()));
        }
        if (topic.getEnterpriseTeacherSignTime() != null) {
            vo.setEnterpriseTeacherSignTime(DATE_FORMAT.format(topic.getEnterpriseTeacherSignTime()));
        }
        if (topic.getCreateTime() != null) {
            vo.setCreateTime(DATE_FORMAT.format(topic.getCreateTime()));
        }
        if (topic.getUpdateTime() != null) {
            vo.setUpdateTime(DATE_FORMAT.format(topic.getUpdateTime()));
        }

        return vo;
    }
}

package com.yuwan.completebackend.service;

import com.yuwan.completebackend.common.PageResult;
import com.yuwan.completebackend.model.dto.BatchReviewDTO;
import com.yuwan.completebackend.model.dto.GeneralOpinionDTO;
import com.yuwan.completebackend.model.dto.ModifyReviewDTO;
import com.yuwan.completebackend.model.dto.ReviewTopicDTO;
import com.yuwan.completebackend.model.vo.*;

import java.util.List;

/**
 * 课题审查服务接口
 * 提供课题审查相关的业务逻辑处理
 *
 * @author 系统架构师
 * @version 1.0
 * @since 2026-02-23
 */
public interface ITopicReviewService {

    /**
     * 获取待审查课题列表
     * 根据当前用户角色自动筛选待审查的课题
     *
     * @param queryVO 查询参数
     * @return 待审查课题分页列表
     */
    PageResult<TopicReviewListVO> getPendingTopics(ReviewQueryVO queryVO);

    /**
     * 单个课题审批
     *
     * @param reviewDTO 审批请求参数
     * @return 审批后的课题信息
     */
    TopicVO reviewTopic(ReviewTopicDTO reviewDTO);

    /**
     * 批量课题审批
     *
     * @param batchDTO 批量审批请求参数
     * @return 批量审批结果
     */
    BatchReviewResultVO batchReviewTopics(BatchReviewDTO batchDTO);

    /**
     * 获取课题审查历史记录
     *
     * @param topicId 课题ID
     * @return 审查历史记录列表
     */
    List<TopicReviewRecordVO> getReviewHistory(String topicId);

    /**
     * 提交综合修改意见
     *
     * @param opinionDTO 综合意见请求参数
     * @return 综合意见信息
     */
    GeneralOpinionVO submitGeneralOpinion(GeneralOpinionDTO opinionDTO);

    /**
     * 获取综合意见列表
     * 获取当前用户专业方向下的综合意见
     *
     * @param reviewStage       审查阶段（可选，不传查全部）
     * @param guidanceDirection 专业方向
     * @return 综合意见列表
     */
    List<GeneralOpinionVO> getGeneralOpinions(Integer reviewStage, String guidanceDirection);

    /**
     * 修改审查结果
     * 符合条件的审查人可以修改自己的审查结果
     *
     * @param modifyDTO 修改审查请求参数
     * @return 修改后的课题信息
     */
    TopicVO modifyReviewResult(ModifyReviewDTO modifyDTO);

    /**
     * 删除综合意见
     *
     * @param opinionId 意见ID
     */
    void deleteGeneralOpinion(String opinionId);

    /**
     * 获取教师通过终审的课题数统计
     *
     * @param teacherId 教师ID（企业教师）
     * @return 统计信息
     */
    TeacherPassedCountVO getTeacherPassedCount(String teacherId);

    /**
     * 检查教师是否可以继续提交课题
     * 判断是否已达到18个上限
     *
     * @param teacherId 教师ID
     * @return 是否可提交
     */
    boolean canTeacherSubmitTopic(String teacherId);

    /**
     * 获取当前用户对指定课题的审查权限
     * 判断当前用户是否有权限审查指定课题
     *
     * @param topicId 课题ID
     * @return 是否有权限审查
     */
    boolean canReviewTopic(String topicId);

    /**
     * 获取当前用户可修改的审查记录
     * 根据业务规则判断哪些审查记录可以被当前用户修改
     *
     * @param topicId 课题ID
     * @return 可修改的审查记录（如果有）
     */
    TopicReviewRecordVO getModifiableReviewRecord(String topicId);
}

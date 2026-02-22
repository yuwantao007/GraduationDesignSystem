package com.yuwan.completebackend.service;

import com.yuwan.completebackend.common.PageResult;
import com.yuwan.completebackend.model.dto.CreateTopicDTO;
import com.yuwan.completebackend.model.dto.SubmitTopicDTO;
import com.yuwan.completebackend.model.dto.TopicSignDTO;
import com.yuwan.completebackend.model.dto.UpdateTopicDTO;
import com.yuwan.completebackend.model.vo.TopicListVO;
import com.yuwan.completebackend.model.vo.TopicQueryVO;
import com.yuwan.completebackend.model.vo.TopicVO;

/**
 * 课题管理服务接口
 * 提供课题申报CRUD、提交、签名等功能
 *
 * @author 系统架构师
 * @version 1.0
 * @since 2026-02-21
 */
public interface ITopicService {

    /**
     * 创建课题（保存为草稿）
     *
     * @param createDTO 创建课题参数
     * @return 课题信息
     */
    TopicVO createTopic(CreateTopicDTO createDTO);

    /**
     * 更新课题信息
     *
     * @param topicId   课题ID
     * @param updateDTO 更新参数
     * @return 更新后的课题信息
     */
    TopicVO updateTopic(String topicId, UpdateTopicDTO updateDTO);

    /**
     * 获取课题详情
     *
     * @param topicId 课题ID
     * @return 课题详细信息
     */
    TopicVO getTopicDetail(String topicId);

    /**
     * 分页查询课题列表
     *
     * @param queryVO 查询参数
     * @return 分页结果
     */
    PageResult<TopicListVO> getTopicList(TopicQueryVO queryVO);

    /**
     * 删除课题（仅草稿或预审前可删除）
     *
     * @param topicId 课题ID
     */
    void deleteTopic(String topicId);

    /**
     * 提交课题申报（草稿 -> 待预审）
     *
     * @param submitDTO 提交参数
     * @return 提交后的课题信息
     */
    TopicVO submitTopic(SubmitTopicDTO submitDTO);

    /**
     * 撤回课题申报（待预审 -> 草稿）
     *
     * @param topicId 课题ID
     * @return 撤回后的课题信息
     */
    TopicVO withdrawTopic(String topicId);

    /**
     * 课题签名（学院负责人/企业负责人/企业指导教师）
     *
     * @param signDTO 签名参数
     * @return 签名后的课题信息
     */
    TopicVO signTopic(TopicSignDTO signDTO);

    /**
     * 获取当前用户创建的课题列表
     *
     * @param queryVO 查询参数
     * @return 分页结果
     */
    PageResult<TopicListVO> getMyTopics(TopicQueryVO queryVO);

    /**
     * 检查课题名称是否已存在
     *
     * @param topicTitle 课题名称
     * @param excludeId  排除的课题ID
     * @return 是否存在
     */
    boolean isTopicTitleExists(String topicTitle, String excludeId);

    /**
     * 统计指定企业教师通过终审的课题数量
     *
     * @param creatorId 创建人ID
     * @return 通过终审的课题数量
     */
    int countPassedTopics(String creatorId);
}

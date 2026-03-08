package com.yuwan.completebackend.service;

import com.yuwan.completebackend.common.PageResult;
import com.yuwan.completebackend.model.dto.ApplyTopicDTO;
import com.yuwan.completebackend.model.vo.TopicForSelectionVO;
import com.yuwan.completebackend.model.vo.TopicSelectionVO;

import java.util.List;

/**
 * 课题选报服务接口
 * 提供学生选报课题的业务功能
 *
 * @author 系统架构师
 * @version 1.0
 * @since 2026-03-08
 */
public interface ITopicSelectionService {

    /**
     * 查询可选课题列表（终审通过的课题，分页）
     *
     * @param topicCategory     课题大类筛选（可选）
     * @param guidanceDirection 指导方向模糊搜索（可选）
     * @param topicTitle        课题名称模糊搜索（可选）
     * @param majorId           专业ID筛选（可选）
     * @param pageNum           页码
     * @param pageSize          每页条数
     * @return 分页课题列表
     */
    PageResult<TopicForSelectionVO> getAvailableTopics(
            Integer topicCategory,
            String guidanceDirection,
            String topicTitle,
            String majorId,
            int pageNum,
            int pageSize
    );

    /**
     * 学生选报课题
     *
     * @param dto 选报参数
     * @return 选报记录
     */
    TopicSelectionVO applyTopic(ApplyTopicDTO dto);

    /**
     * 删除选报记录（仅落选后可删除）
     *
     * @param selectionId 选报记录ID
     */
    void deleteSelection(String selectionId);

    /**
     * 查询当前学生的选报记录列表
     *
     * @return 选报记录列表
     */
    List<TopicSelectionVO> getMySelections();
}

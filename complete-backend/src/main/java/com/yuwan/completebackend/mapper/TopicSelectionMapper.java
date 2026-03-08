package com.yuwan.completebackend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yuwan.completebackend.model.entity.TopicSelection;
import com.yuwan.completebackend.model.vo.TopicForSelectionVO;
import com.yuwan.completebackend.model.vo.TopicSelectionVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 课题选报记录 Mapper
 *
 * @author 系统架构师
 * @version 1.0
 * @since 2026-03-08
 */
@Mapper
public interface TopicSelectionMapper extends BaseMapper<TopicSelection> {

    /**
     * 查询可选课题列表（终审通过的课题）
     *
     * @param majorId            专业ID筛选（可选）
     * @param topicCategory      课题大类筛选（可选）
     * @param guidanceDirection  指导方向模糊搜索（可选）
     * @param topicTitle         课题名称模糊搜索（可选）
     * @param studentId          当前学生ID（用于判断是否已选报）
     * @return 可选课题列表
     */
    List<TopicForSelectionVO> selectAvailableTopics(
            @Param("majorId") String majorId,
            @Param("topicCategory") Integer topicCategory,
            @Param("guidanceDirection") String guidanceDirection,
            @Param("topicTitle") String topicTitle,
            @Param("studentId") String studentId
    );

    /**
     * 查询学生的选报记录列表（含课题、企业信息）
     *
     * @param studentId 学生ID
     * @return 选报记录列表
     */
    List<TopicSelectionVO> selectMySelections(@Param("studentId") String studentId);
}

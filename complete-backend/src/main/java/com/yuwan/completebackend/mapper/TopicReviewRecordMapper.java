package com.yuwan.completebackend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yuwan.completebackend.model.entity.TopicReviewRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 课题审查记录Mapper接口
 *
 * @author 系统架构师
 * @version 1.0
 * @since 2026-02-23
 */
@Mapper
public interface TopicReviewRecordMapper extends BaseMapper<TopicReviewRecord> {

    /**
     * 根据课题ID查询审查历史记录
     *
     * @param topicId 课题ID
     * @return 审查记录列表，按时间倒序
     */
    List<TopicReviewRecord> selectByTopicIdOrderByTime(@Param("topicId") String topicId);

    /**
     * 根据课题ID和审查阶段查询最新审查记录
     *
     * @param topicId     课题ID
     * @param reviewStage 审查阶段
     * @return 最新审查记录
     */
    TopicReviewRecord selectLatestByTopicAndStage(@Param("topicId") String topicId,
                                                   @Param("reviewStage") Integer reviewStage);

    /**
     * 统计课题的审查记录数
     *
     * @param topicId 课题ID
     * @return 审查记录数
     */
    Integer countByTopicId(@Param("topicId") String topicId);
}

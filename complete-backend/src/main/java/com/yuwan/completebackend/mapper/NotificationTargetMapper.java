package com.yuwan.completebackend.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 站内消息接收对象查询Mapper
 *
 * @author 系统架构师
 * @version 1.0
 * @since 2026-03-21
 */
@Mapper
public interface NotificationTargetMapper {

    /**
     * 查询答辩安排范围内的中选学生
     */
    @Select("<script>" +
            "SELECT DISTINCT x.student_id FROM (" +
            "  SELECT ts.student_id AS student_id " +
            "  FROM topic_selection ts " +
            "  JOIN topic_info t ON ts.topic_id = t.topic_id AND t.deleted = 0 " +
            "  JOIN user_info u ON ts.student_id = u.user_id AND u.deleted = 0 " +
            "  WHERE ts.deleted = 0 " +
            "  AND ts.selection_status = 1 " +
            "  AND t.enterprise_id = #{enterpriseId} " +
            "  <if test='topicCategory != null'> AND t.topic_category = #{topicCategory} </if>" +
            "  <if test='majorId != null and majorId != \"\"'> AND t.major_id = #{majorId} </if>" +
            "  UNION " +
            "  SELECT tb.student_id AS student_id " +
            "  FROM opening_task_book tb " +
            "  JOIN topic_info t ON tb.topic_id = t.topic_id AND t.deleted = 0 " +
            "  JOIN user_info u ON tb.student_id = u.user_id AND u.deleted = 0 " +
            "  WHERE tb.is_deleted = 0 " +
            "  AND t.enterprise_id = #{enterpriseId} " +
            "  <if test='topicCategory != null'> AND t.topic_category = #{topicCategory} </if>" +
            "  <if test='majorId != null and majorId != \"\"'> AND t.major_id = #{majorId} </if>" +
            ") x " +
            "WHERE x.student_id IS NOT NULL " +
            "</script>")
    List<String> selectStudentIdsForArrangement(@Param("enterpriseId") String enterpriseId,
                                                @Param("topicCategory") Integer topicCategory,
                                                @Param("majorId") String majorId);

    /**
     * 查询答辩安排范围内的企业指导教师
     */
    @Select("<script>" +
            "SELECT DISTINCT t.creator_id " +
            "FROM topic_selection ts " +
            "JOIN topic_info t ON ts.topic_id = t.topic_id AND t.deleted = 0 " +
            "WHERE ts.deleted = 0 " +
            "AND ts.selection_status = 1 " +
            "AND t.enterprise_id = #{enterpriseId} " +
            "<if test='topicCategory != null'> AND t.topic_category = #{topicCategory} </if>" +
            "<if test='majorId != null and majorId != \"\"'> AND t.major_id = #{majorId} </if>" +
            "</script>")
    List<String> selectEnterpriseTeacherIdsForArrangement(@Param("enterpriseId") String enterpriseId,
                                                          @Param("topicCategory") Integer topicCategory,
                                                          @Param("majorId") String majorId);
}

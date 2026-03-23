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

    /**
     * 根据企业教师列表查询已启用配对关系的高校教师（可按届别过滤）
     */
    @Select("<script>" +
            "SELECT DISTINCT tr.univ_teacher_id " +
            "FROM teacher_relationship tr " +
            "JOIN user_info u ON u.user_id = tr.univ_teacher_id AND u.deleted = 0 " +
            "WHERE tr.deleted = 0 " +
            "AND tr.is_enabled = 1 " +
            "<if test='cohort != null and cohort != \"\"'> AND tr.cohort = #{cohort} </if>" +
            "<if test='enterpriseTeacherIds != null and enterpriseTeacherIds.size() > 0'>" +
            "  AND tr.enterprise_teacher_id IN " +
            "  <foreach collection='enterpriseTeacherIds' item='teacherId' open='(' separator=',' close=')'>" +
            "    #{teacherId}" +
            "  </foreach>" +
            "</if>" +
            "</script>")
    List<String> selectUniversityTeacherIdsByEnterpriseTeachers(@Param("enterpriseTeacherIds") List<String> enterpriseTeacherIds,
                                                                 @Param("cohort") String cohort);

    /**
     * 判断高校教师是否与指定企业教师列表存在已启用配对关系（可按届别过滤）
     */
    @Select("<script>" +
            "SELECT COUNT(1) " +
            "FROM teacher_relationship tr " +
            "WHERE tr.deleted = 0 " +
            "AND tr.is_enabled = 1 " +
            "AND tr.univ_teacher_id = #{univTeacherId} " +
            "<if test='cohort != null and cohort != \"\"'> AND tr.cohort = #{cohort} </if>" +
            "<if test='enterpriseTeacherIds != null and enterpriseTeacherIds.size() > 0'>" +
            "  AND tr.enterprise_teacher_id IN " +
            "  <foreach collection='enterpriseTeacherIds' item='teacherId' open='(' separator=',' close=')'>" +
            "    #{teacherId}" +
            "  </foreach>" +
            "</if>" +
            "</script>")
    Integer countUniversityTeacherRelationForTeachers(@Param("univTeacherId") String univTeacherId,
                                                      @Param("enterpriseTeacherIds") List<String> enterpriseTeacherIds,
                                                      @Param("cohort") String cohort);
}

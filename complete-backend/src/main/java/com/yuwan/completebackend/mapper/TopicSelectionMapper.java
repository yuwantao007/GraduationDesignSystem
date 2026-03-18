package com.yuwan.completebackend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yuwan.completebackend.model.entity.TopicSelection;
import com.yuwan.completebackend.model.vo.SelectionForTeacherVO;
import com.yuwan.completebackend.model.vo.SelectionForUnivTeacherVO;
import com.yuwan.completebackend.model.vo.SelectionOverviewVO;
import com.yuwan.completebackend.model.vo.TopicForSelectionVO;
import com.yuwan.completebackend.model.vo.TopicSelectionVO;
import com.yuwan.completebackend.model.vo.UnselectedStudentVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

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
            @Param("enterpriseId") String enterpriseId,
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

    /**
     * 查询选报了指定教师课题的学生记录（企业教师视角）
     *
     * @param teacherId      企业教师ID
     * @param selectionStatus 选报状态过滤（null=全部）
     * @return 选报记录列表
     */
    List<SelectionForTeacherVO> selectByTeacher(
            @Param("teacherId") String teacherId,
            @Param("selectionStatus") Integer selectionStatus
    );

    /**
     * 查询企业内各课题的双选概览（企业负责人视角）
     *
     * @param enterpriseId 企业ID
     * @return 双选概览列表
     */
    List<SelectionOverviewVO> selectOverviewByEnterprise(@Param("enterpriseId") String enterpriseId);

    /**
     * 查询企业内未选报任何课题的学生（企业负责人视角）
     *
     * @param enterpriseId 企业ID
     * @return 未选报学生列表
     */
    List<UnselectedStudentVO> selectUnselectedStudents(@Param("enterpriseId") String enterpriseId);

    /**
     * 查询高校教师配对企业教师名下学生的选报结果
     * <p>
     * 通过 teacher_relationship 找到当前高校教师对应的企业教师列表，
     * 再查询这些企业教师所创建课题的全部选报记录。
     * </p>
     *
     * @param univTeacherId   高校教师用户ID
     * @param selectionStatus 选报状态过滤（null=全部，0=待确认，1=中选，2=落选）
     * @return 选报结果列表
     */
    List<SelectionForUnivTeacherVO> selectByUnivTeacher(
            @Param("univTeacherId") String univTeacherId,
            @Param("selectionStatus") Integer selectionStatus
    );

    /**
     * 统计至少有一条有效选报记录的学生数（去重）
     * <p>
     * 使用 COUNT(DISTINCT) 替代 selectCount + groupBy，避免 MyBatis Plus
     * selectCount 与 GROUP BY 配合时因多行结果无法映射为标量而返回 null 的问题。
     * </p>
     *
     * @return 有选报记录的学生去重数量
     */
    @Select("SELECT COUNT(DISTINCT student_id) FROM topic_selection WHERE deleted = 0")
    long countDistinctStudentsWithSelection();
}

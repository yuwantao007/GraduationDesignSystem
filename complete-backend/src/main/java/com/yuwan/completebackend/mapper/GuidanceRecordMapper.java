package com.yuwan.completebackend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yuwan.completebackend.model.entity.GuidanceRecord;
import com.yuwan.completebackend.model.vo.GuidanceListVO;
import com.yuwan.completebackend.model.vo.GuidanceRecordVO;
import com.yuwan.completebackend.model.vo.GuidanceStudentVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 指导记录数据访问层
 *
 * @author 系统架构师
 * @version 1.0
 * @since 2026-03-16
 */
@Mapper
public interface GuidanceRecordMapper extends BaseMapper<GuidanceRecord> {

    /**
     * 查询指导记录详情（含学生、教师、课题信息）
     *
     * @param recordId 记录ID
     * @return 指导记录详情
     */
    GuidanceRecordVO selectRecordDetail(@Param("recordId") String recordId);

    /**
     * 查询某学生的全部指导记录
     *
     * @param studentId 学生ID
     * @param guidanceType 指导类型（可选）
     * @return 指导记录列表
     */
    List<GuidanceListVO> selectByStudentId(@Param("studentId") String studentId,
                                           @Param("guidanceType") Integer guidanceType);

    /**
     * 查询教师视角的学生列表（含最新指导时间和统计）
     *
     * @param teacherId 教师ID
     * @param guidanceType 指导类型（1-项目指导 2-论文指导）
     * @return 学生列表
     */
    List<GuidanceStudentVO> selectStudentsByTeacher(@Param("teacherId") String teacherId,
                                                    @Param("guidanceType") Integer guidanceType);

    /**
     * 查询企业负责人视角的指导记录总览（支持筛选分页）
     *
     * @param enterpriseId 企业ID
     * @param studentName 学生姓名（模糊）
     * @param teacherId 教师ID
     * @param guidanceType 指导类型
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 指导记录列表
     */
    List<GuidanceListVO> selectByEnterprise(@Param("enterpriseId") String enterpriseId,
                                            @Param("studentName") String studentName,
                                            @Param("teacherId") String teacherId,
                                            @Param("guidanceType") Integer guidanceType,
                                            @Param("startDate") String startDate,
                                            @Param("endDate") String endDate);

    /**
     * 统计企业内指导记录数量
     *
     * @param enterpriseId 企业ID
     * @return 记录数
     */
    Long countByEnterprise(@Param("enterpriseId") String enterpriseId);

    /**
     * 高校教师查询配对企业教师名下学生的指导记录
     *
     * @param univTeacherId 高校教师ID
     * @param guidanceType 指导类型
     * @return 指导记录列表
     */
    List<GuidanceListVO> selectByUnivTeacher(@Param("univTeacherId") String univTeacherId,
                                             @Param("guidanceType") Integer guidanceType);
}

package com.yuwan.completebackend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yuwan.completebackend.model.dto.midterm.MidtermCheckQueryDTO;
import com.yuwan.completebackend.model.entity.MidtermCheck;
import com.yuwan.completebackend.model.vo.MidtermCheckListVO;
import com.yuwan.completebackend.model.vo.MidtermCheckVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 中期检查表Mapper接口
 *
 * @author 系统架构师
 * @version 1.0
 * @since 2026-03-17
 */
@Mapper
public interface MidtermCheckMapper extends BaseMapper<MidtermCheck> {

    /**
     * 根据ID查询详情（含关联信息）
     *
     * @param checkId 检查表ID
     * @return 详情VO
     */
    MidtermCheckVO selectDetailById(@Param("checkId") String checkId);

    /**
     * 根据学生ID查询详情
     *
     * @param studentId 学生ID
     * @return 详情VO
     */
    MidtermCheckVO selectByStudentId(@Param("studentId") String studentId);

    /**
     * 条件查询列表
     *
     * @param queryDTO 查询条件
     * @return 列表VO
     */
    List<MidtermCheckListVO> selectListByCondition(@Param("query") MidtermCheckQueryDTO queryDTO);

    /**
     * 统计条件查询数量
     *
     * @param queryDTO 查询条件
     * @return 数量
     */
    Long countByCondition(@Param("query") MidtermCheckQueryDTO queryDTO);

    /**
     * 查询企业教师负责的中期检查列表
     *
     * @param teacherId 企业教师ID
     * @param queryDTO 查询条件
     * @return 列表VO
     */
    List<MidtermCheckListVO> selectByEnterpriseTeacher(@Param("teacherId") String teacherId,
                                                       @Param("query") MidtermCheckQueryDTO queryDTO);

    /**
     * 统计企业教师负责的中期检查数量
     *
     * @param teacherId 企业教师ID
     * @param queryDTO 查询条件
     * @return 数量
     */
    Long countByEnterpriseTeacher(@Param("teacherId") String teacherId,
                                  @Param("query") MidtermCheckQueryDTO queryDTO);

    /**
     * 查询高校教师负责审查的中期检查列表
     *
     * @param teacherId 高校教师ID
     * @param queryDTO 查询条件
     * @return 列表VO
     */
    List<MidtermCheckListVO> selectByUnivTeacher(@Param("teacherId") String teacherId,
                                                 @Param("query") MidtermCheckQueryDTO queryDTO);

    /**
     * 统计高校教师负责审查的中期检查数量
     *
     * @param teacherId 高校教师ID
     * @param queryDTO 查询条件
     * @return 数量
     */
    Long countByUnivTeacher(@Param("teacherId") String teacherId,
                            @Param("query") MidtermCheckQueryDTO queryDTO);
}

package com.yuwan.completebackend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yuwan.completebackend.model.entity.TeacherAssignment;
import com.yuwan.completebackend.model.vo.TeacherAssignmentVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 教师指派记录 Mapper
 * <p>单表 CRUD 全部使用 MyBatis-Plus BaseMapper + QueryWrapper</p>
 *
 * @author 系统架构师
 * @version 1.0
 * @since 2026-03-14
 */
@Mapper
public interface TeacherAssignmentMapper extends BaseMapper<TeacherAssignment> {

    /**
     * 查询企业内的指派列表（联表查询，含学生/课题/教师姓名）
     *
     * @param enterpriseId 企业ID（企业负责人的企业范围）
     * @return 指派记录列表
     */
    List<TeacherAssignmentVO> selectAssignmentList(@Param("enterpriseId") String enterpriseId);
}

package com.yuwan.completebackend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yuwan.completebackend.model.entity.TeacherRelationship;
import com.yuwan.completebackend.model.vo.UnivTeacherPairingVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 高校教师-企业教师精确配对 Mapper
 *
 * @author 系统架构师
 * @version 1.0
 * @since 2026-03-08
 */
@Mapper
public interface TeacherRelationshipMapper extends BaseMapper<TeacherRelationship> {

    /**
     * 查询高校教师的所有配对关系（含企业教师、企业、方向、选报统计）
     *
     * @param univTeacherId 高校教师用户ID
     * @return 配对信息列表
     */
    List<UnivTeacherPairingVO> selectPairingsByUnivTeacher(@Param("univTeacherId") String univTeacherId);
}

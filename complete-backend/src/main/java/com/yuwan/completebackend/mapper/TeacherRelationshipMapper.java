package com.yuwan.completebackend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yuwan.completebackend.model.entity.TeacherRelationship;
import org.apache.ibatis.annotations.Mapper;

/**
 * 高校教师-企业教师精确配对 Mapper
 *
 * @author 系统架构师
 * @version 1.0
 * @since 2026-03-08
 */
@Mapper
public interface TeacherRelationshipMapper extends BaseMapper<TeacherRelationship> {
}

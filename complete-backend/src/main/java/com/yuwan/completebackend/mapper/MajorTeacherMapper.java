package com.yuwan.completebackend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yuwan.completebackend.model.entity.MajorTeacher;
import org.apache.ibatis.annotations.Mapper;

/**
 * 专业-企业老师关联Mapper接口
 * <p>单表 CRUD 全部使用 MyBatis-Plus BaseMapper + QueryWrapper</p>
 *
 * @author 系统架构师
 * @version 1.0
 * @since 2026-03-07
 */
@Mapper
public interface MajorTeacherMapper extends BaseMapper<MajorTeacher> {
}

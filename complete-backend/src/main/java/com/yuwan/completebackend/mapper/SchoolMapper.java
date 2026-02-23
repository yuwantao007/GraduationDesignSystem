package com.yuwan.completebackend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yuwan.completebackend.model.entity.School;
import org.apache.ibatis.annotations.Mapper;

/**
 * 学校信息Mapper接口
 * 继承MyBatis-Plus BaseMapper，提供基础CRUD功能
 *
 * @author 系统架构师
 * @version 1.0
 * @since 2026-02-22
 */
@Mapper
public interface SchoolMapper extends BaseMapper<School> {
    // 使用 MyBatis-Plus BaseMapper 提供的基础方法
    // 如需自定义SQL，可在此添加接口方法并在 SchoolMapper.xml 中实现
}

package com.yuwan.completebackend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yuwan.completebackend.model.entity.DocumentAccessLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 文档访问日志Mapper接口
 *
 * @author 系统架构师
 * @version 1.0
 * @since 2026-03-15
 */
@Mapper
public interface DocumentAccessLogMapper extends BaseMapper<DocumentAccessLog> {
    // 使用MyBatis-Plus的基础CRUD方法即可
}

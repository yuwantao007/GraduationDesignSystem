package com.yuwan.completebackend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yuwan.completebackend.model.entity.SystemPhaseConfig;
import org.apache.ibatis.annotations.Mapper;

/**
 * 系统阶段配置Mapper接口
 * <p>单表 CRUD 优先使用 MyBatis-Plus BaseMapper + QueryWrapper</p>
 *
 * @author 系统架构师
 * @version 1.0
 * @since 2026-03-07
 */
@Mapper
public interface SystemPhaseConfigMapper extends BaseMapper<SystemPhaseConfig> {

}

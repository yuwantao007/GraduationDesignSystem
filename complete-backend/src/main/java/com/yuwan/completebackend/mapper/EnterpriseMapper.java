package com.yuwan.completebackend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yuwan.completebackend.model.entity.Enterprise;
import org.apache.ibatis.annotations.Mapper;

/**
 * 企业信息Mapper接口
 * <p>单表 CRUD 优先使用 MyBatis-Plus BaseMapper + QueryWrapper</p>
 *
 * @author 系统架构师
 * @version 1.0
 * @since 2026-02-21
 */
@Mapper
public interface EnterpriseMapper extends BaseMapper<Enterprise> {

}

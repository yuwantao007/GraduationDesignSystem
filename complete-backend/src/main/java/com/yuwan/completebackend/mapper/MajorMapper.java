package com.yuwan.completebackend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yuwan.completebackend.model.entity.Major;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 专业Mapper接口
 * <p>单表 CRUD 优先使用 MyBatis-Plus BaseMapper + QueryWrapper</p>
 *
 * @author 系统架构师
 * @version 1.0
 * @since 2026-03-01
 */
@Mapper
public interface MajorMapper extends BaseMapper<Major> {

    /**
     * 统计指定专业方向下的专业数量
     *
     * @param directionId 专业方向ID
     * @return 专业数量
     */
    @Select("SELECT COUNT(*) FROM major_info WHERE direction_id = #{directionId} AND deleted = 0")
    Integer countByDirectionId(@Param("directionId") String directionId);

    /**
     * 统计指定企业的专业数量
     *
     * @param enterpriseId 企业ID
     * @return 专业数量
     */
    @Select("SELECT COUNT(*) FROM major_info WHERE enterprise_id = #{enterpriseId} AND deleted = 0")
    Integer countByEnterpriseId(@Param("enterpriseId") String enterpriseId);
}

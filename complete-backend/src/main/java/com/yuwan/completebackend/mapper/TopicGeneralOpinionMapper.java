package com.yuwan.completebackend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yuwan.completebackend.model.entity.TopicGeneralOpinion;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 综合审查意见Mapper接口
 *
 * @author 系统架构师
 * @version 1.0
 * @since 2026-02-23
 */
@Mapper
public interface TopicGeneralOpinionMapper extends BaseMapper<TopicGeneralOpinion> {

    /**
     * 根据专业方向和审查阶段查询综合意见
     *
     * @param guidanceDirection 专业方向
     * @param reviewStage       审查阶段（可选）
     * @return 综合意见列表
     */
    List<TopicGeneralOpinion> selectByDirectionAndStage(@Param("guidanceDirection") String guidanceDirection,
                                                         @Param("reviewStage") Integer reviewStage);
}

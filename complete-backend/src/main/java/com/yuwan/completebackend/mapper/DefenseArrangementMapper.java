package com.yuwan.completebackend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yuwan.completebackend.model.entity.DefenseArrangement;
import com.yuwan.completebackend.model.vo.defense.DefenseArrangementVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 答辩安排Mapper接口
 *
 * @author 系统架构师
 * @version 1.0
 * @since 2026-03-17
 */
@Mapper
public interface DefenseArrangementMapper extends BaseMapper<DefenseArrangement> {

    /**
     * 分页查询答辩安排列表（带企业信息）
     *
     * @param page         分页参数
     * @param enterpriseId 企业ID
     * @param defenseType  答辩类型
     * @param topicCategory 课题类别
     * @param cohort       毕业届别
     * @param status       状态
     * @return 分页结果
     */
    IPage<DefenseArrangementVO> selectArrangementPage(
            Page<DefenseArrangementVO> page,
            @Param("enterpriseId") String enterpriseId,
            @Param("defenseType") Integer defenseType,
            @Param("topicCategory") String topicCategory,
            @Param("cohort") String cohort,
            @Param("status") Integer status
    );

    /**
     * 根据ID查询答辩安排详情
     *
     * @param arrangementId 安排ID
     * @return 答辩安排详情
     */
    DefenseArrangementVO selectArrangementById(@Param("arrangementId") String arrangementId);
}

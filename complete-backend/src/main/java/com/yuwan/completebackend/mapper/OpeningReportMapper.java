package com.yuwan.completebackend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yuwan.completebackend.model.entity.OpeningReport;
import com.yuwan.completebackend.model.vo.defense.OpeningReportVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 开题报告Mapper接口
 *
 * @author 系统架构师
 * @version 1.0
 * @since 2026-03-17
 */
@Mapper
public interface OpeningReportMapper extends BaseMapper<OpeningReport> {

    /**
     * 分页查询开题报告列表
     *
     * @param page          分页参数
     * @param enterpriseId  企业ID
     * @param studentName   学生姓名（模糊）
     * @param reviewStatus  审查状态
     * @param arrangementId 答辩安排ID
     * @return 分页结果
     */
    IPage<OpeningReportVO> selectReportPage(
            Page<OpeningReportVO> page,
            @Param("enterpriseId") String enterpriseId,
            @Param("studentName") String studentName,
            @Param("reviewStatus") Integer reviewStatus,
            @Param("arrangementId") String arrangementId
    );

    /**
     * 根据ID查询开题报告详情
     *
     * @param reportId 报告ID
     * @return 开题报告详情
     */
    OpeningReportVO selectReportById(@Param("reportId") String reportId);

    /**
     * 根据学生ID查询开题报告
     *
     * @param studentId 学生ID
     * @return 开题报告详情
     */
    OpeningReportVO selectReportByStudentId(@Param("studentId") String studentId);
}

package com.yuwan.completebackend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yuwan.completebackend.model.entity.OpeningTaskBook;
import com.yuwan.completebackend.model.vo.defense.OpeningTaskBookVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 开题任务书Mapper接口
 *
 * @author 系统架构师
 * @version 1.0
 * @since 2026-03-17
 */
@Mapper
public interface OpeningTaskBookMapper extends BaseMapper<OpeningTaskBook> {

    /**
     * 分页查询任务书列表
     *
     * @param page         分页参数
     * @param enterpriseId 企业ID
     * @param studentName  学生姓名（模糊）
     * @return 分页结果
     */
    IPage<OpeningTaskBookVO> selectTaskBookPage(
            Page<OpeningTaskBookVO> page,
            @Param("enterpriseId") String enterpriseId,
            @Param("studentName") String studentName
    );

    /**
     * 根据ID查询任务书详情
     *
     * @param taskBookId 任务书ID
     * @return 任务书详情
     */
    OpeningTaskBookVO selectTaskBookById(@Param("taskBookId") String taskBookId);

    /**
     * 根据学生ID查询任务书
     *
     * @param studentId 学生ID
     * @return 任务书详情
     */
    OpeningTaskBookVO selectTaskBookByStudentId(@Param("studentId") String studentId);
}

package com.yuwan.completebackend.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yuwan.completebackend.model.dto.defense.*;
import com.yuwan.completebackend.model.entity.DefenseArrangement;
import com.yuwan.completebackend.model.entity.OpeningReport;
import com.yuwan.completebackend.model.entity.OpeningTaskBook;
import com.yuwan.completebackend.model.vo.defense.DefenseArrangementVO;
import com.yuwan.completebackend.model.vo.defense.OpeningReportVO;
import com.yuwan.completebackend.model.vo.defense.OpeningTaskBookVO;

/**
 * 开题答辩管理Service接口
 *
 * @author 系统架构师
 * @version 1.0
 * @since 2026-03-17
 */
public interface IDefenseService extends IService<DefenseArrangement> {

    // ==================== 答辩安排管理 ====================

    /**
     * 创建答辩安排（企业负责人）
     *
     * @param dto 创建答辩安排DTO
     * @return 新创建的答辩安排ID
     */
    String createArrangement(CreateArrangementDTO dto);

    /**
     * 更新答辩安排（企业负责人）
     *
     * @param dto 更新答辩安排DTO
     * @return 是否成功
     */
    boolean updateArrangement(UpdateArrangementDTO dto);

    /**
     * 删除答辩安排（企业负责人）
     *
     * @param arrangementId 安排ID
     * @return 是否成功
     */
    boolean deleteArrangement(String arrangementId);

    /**
     * 分页查询答辩安排列表
     *
     * @param queryDTO 查询条件
     * @return 分页结果
     */
    IPage<DefenseArrangementVO> pageArrangements(ArrangementQueryDTO queryDTO);

    /**
     * 获取答辩安排详情
     *
     * @param arrangementId 安排ID
     * @return 答辩安排详情
     */
    DefenseArrangementVO getArrangementDetail(String arrangementId);

    // ==================== 开题任务书管理 ====================

    /**
     * 保存/更新任务书（企业教师）
     *
     * @param dto 保存任务书DTO
     * @return 任务书ID
     */
    String saveTaskBook(SaveTaskBookDTO dto);

    /**
     * 获取学生任务书详情
     *
     * @param studentId 学生ID
     * @return 任务书详情
     */
    OpeningTaskBookVO getTaskBookByStudent(String studentId);

    /**
     * 获取任务书详情
     *
     * @param taskBookId 任务书ID
     * @return 任务书详情
     */
    OpeningTaskBookVO getTaskBookDetail(String taskBookId);

    // ==================== 开题报告管理 ====================

    /**
     * 学生提交开题报告
     *
     * @param dto 提交开题报告DTO
     * @return 报告ID
     */
    String submitReport(SubmitReportDTO dto);

    /**
     * 获取学生开题报告详情（学生查看自己的）
     *
     * @return 开题报告详情
     */
    OpeningReportVO getMyReport();

    /**
     * 分页查询开题报告列表（企业教师）
     *
     * @param queryDTO 查询条件
     * @return 分页结果
     */
    IPage<OpeningReportVO> pageReports(ReportQueryDTO queryDTO);

    /**
     * 获取开题报告详情
     *
     * @param reportId 报告ID
     * @return 开题报告详情
     */
    OpeningReportVO getReportDetail(String reportId);

}

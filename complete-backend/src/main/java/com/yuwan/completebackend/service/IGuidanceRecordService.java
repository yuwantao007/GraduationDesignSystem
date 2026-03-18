package com.yuwan.completebackend.service;

import com.yuwan.completebackend.common.PageResult;
import com.yuwan.completebackend.model.dto.CreateGuidanceDTO;
import com.yuwan.completebackend.model.vo.GuidanceListVO;
import com.yuwan.completebackend.model.vo.GuidanceQueryVO;
import com.yuwan.completebackend.model.vo.GuidanceRecordVO;
import com.yuwan.completebackend.model.vo.GuidanceStudentVO;

import java.util.List;

/**
 * 指导记录服务接口
 * 提供企业教师项目指导、高校教师论文指导的全流程业务功能
 *
 * @author 系统架构师
 * @version 1.0
 * @since 2026-03-16
 */
public interface IGuidanceRecordService {

    // ==================== 通用接口 ====================

    /**
     * 新增指导记录（企业教师/高校教师）
     *
     * @param dto 创建参数
     * @return 创建的指导记录
     */
    GuidanceRecordVO createGuidanceRecord(CreateGuidanceDTO dto);

    /**
     * 删除指导记录（本人可删）
     *
     * @param recordId 记录ID
     */
    void deleteGuidanceRecord(String recordId);

    /**
     * 查询指导记录详情
     *
     * @param recordId 记录ID
     * @return 指导记录详情
     */
    GuidanceRecordVO getGuidanceRecordDetail(String recordId);

    // ==================== 教师视角 ====================

    /**
     * 我的学生列表（教师视角，含最新指导时间）
     * <p>
     * 企业教师：查看中选自己课题的学生
     * 高校教师：查看配对企业教师名下的学生
     * </p>
     *
     * @return 学生列表
     */
    List<GuidanceStudentVO> getMyStudents();

    /**
     * 查看某学生的全部指导记录
     *
     * @param studentId 学生ID
     * @return 指导记录列表
     */
    List<GuidanceListVO> getStudentGuidanceRecords(String studentId);

    // ==================== 学生视角 ====================

    /**
     * 我的被指导记录（学生视角）
     *
     * @return 指导记录列表
     */
    List<GuidanceListVO> getMyGuidanceRecords();

    // ==================== 企业负责人视角 ====================

    /**
     * 指导记录总览（企业负责人，支持筛选分页）
     *
     * @param queryVO 查询条件
     * @return 分页结果
     */
    PageResult<GuidanceListVO> getLeaderGuidanceOverview(GuidanceQueryVO queryVO);

    /**
     * 导出指导记录 Excel（企业负责人）
     *
     * @param queryVO 查询条件
     * @return Excel 字节数组
     */
    byte[] exportGuidanceRecords(GuidanceQueryVO queryVO);
}

package com.yuwan.completebackend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yuwan.completebackend.common.PageResult;
import com.yuwan.completebackend.model.dto.midterm.CreateMidtermCheckDTO;
import com.yuwan.completebackend.model.dto.midterm.MidtermCheckQueryDTO;
import com.yuwan.completebackend.model.dto.midterm.ReviewMidtermCheckDTO;
import com.yuwan.completebackend.model.entity.MidtermCheck;
import com.yuwan.completebackend.model.vo.MidtermCheckListVO;
import com.yuwan.completebackend.model.vo.MidtermCheckVO;

/**
 * 中期检查服务接口
 *
 * @author 系统架构师
 * @version 1.0
 * @since 2026-03-17
 */
public interface IMidtermCheckService extends IService<MidtermCheck> {

    /**
     * 企业教师创建/编辑中期检查表
     *
     * @param dto 创建/编辑DTO
     * @param teacherId 企业教师ID
     * @return 检查表ID
     */
    String createOrUpdateCheck(CreateMidtermCheckDTO dto, String teacherId);

    /**
     * 企业教师提交中期检查表
     *
     * @param checkId 检查表ID
     * @param teacherId 企业教师ID
     * @return 操作结果
     */
    Boolean submitCheck(String checkId, String teacherId);

    /**
     * 高校教师审查中期检查表
     *
     * @param dto 审查DTO
     * @param reviewerId 高校教师ID
     * @return 操作结果
     */
    Boolean reviewCheck(ReviewMidtermCheckDTO dto, String reviewerId);

    /**
     * 获取中期检查表详情
     *
     * @param checkId 检查表ID
     * @return 详情VO
     */
    MidtermCheckVO getCheckDetail(String checkId);

    /**
     * 获取学生的中期检查表
     *
     * @param studentId 学生ID
     * @return 详情VO
     */
    MidtermCheckVO getByStudentId(String studentId);

    /**
     * 企业教师查询自己负责的中期检查列表
     *
     * @param teacherId 企业教师ID
     * @param queryDTO 查询条件
     * @return 分页结果
     */
    PageResult<MidtermCheckListVO> getEnterpriseTeacherList(String teacherId, MidtermCheckQueryDTO queryDTO);

    /**
     * 高校教师查询自己负责审查的中期检查列表
     *
     * @param teacherId 高校教师ID
     * @param queryDTO 查询条件
     * @return 分页结果
     */
    PageResult<MidtermCheckListVO> getUnivTeacherList(String teacherId, MidtermCheckQueryDTO queryDTO);

    /**
     * 管理员查询所有中期检查列表
     *
     * @param queryDTO 查询条件
     * @return 分页结果
     */
    PageResult<MidtermCheckListVO> getAdminList(MidtermCheckQueryDTO queryDTO);

    /**
     * 初始化学生的中期检查表（系统自动创建）
     *
     * @param studentId 学生ID
     * @param topicId 课题ID
     * @param enterpriseTeacherId 企业教师ID
     * @return 检查表ID
     */
    String initCheckForStudent(String studentId, String topicId, String enterpriseTeacherId);
}

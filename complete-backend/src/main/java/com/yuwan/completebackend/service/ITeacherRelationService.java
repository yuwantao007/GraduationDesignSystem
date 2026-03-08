package com.yuwan.completebackend.service;

import com.yuwan.completebackend.model.dto.TeacherRelationDTO;
import com.yuwan.completebackend.model.dto.UnivTeacherMajorDTO;
import com.yuwan.completebackend.model.vo.TeacherCoverageVO;
import com.yuwan.completebackend.model.vo.TeacherRelationVO;
import com.yuwan.completebackend.model.vo.UnivTeacherMajorVO;

import java.util.List;
import java.util.Map;

/**
 * 教师配对管理服务接口
 * <p>
 * 提供高校教师与企业教师/专业方向的双层配对管理功能：
 * <ul>
 *   <li>第一层（粗粒度）：高校教师 → 专业方向</li>
 *   <li>第二层（细粒度）：高校教师 ↔ 企业教师</li>
 * </ul>
 * 查找时优先使用第二层，兜底使用第一层。
 * </p>
 *
 * @author 系统架构师
 * @version 1.0
 * @since 2026-03-08
 */
public interface ITeacherRelationService {

    // ==================== 第一层：方向级分配 ====================

    /**
     * 查询方向级分配列表
     *
     * @param enterpriseId 企业ID（可选）
     * @param cohort       届别（可选）
     * @return 分配列表
     */
    List<UnivTeacherMajorVO> listMajorAssignments(String enterpriseId, String cohort);

    /**
     * 新增方向级分配
     *
     * @param dto 分配参数
     * @return 分配结果
     */
    UnivTeacherMajorVO addMajorAssignment(UnivTeacherMajorDTO dto);

    /**
     * 编辑方向级分配
     *
     * @param id  分配ID
     * @param dto 修改参数
     * @return 修改结果
     */
    UnivTeacherMajorVO updateMajorAssignment(String id, UnivTeacherMajorDTO dto);

    /**
     * 删除方向级分配
     *
     * @param id 分配ID
     */
    void deleteMajorAssignment(String id);

    // ==================== 第二层：精确配对 ====================

    /**
     * 查询精确配对列表
     *
     * @param enterpriseId 企业ID（可选）
     * @param cohort       届别（可选）
     * @return 配对列表
     */
    List<TeacherRelationVO> listTeacherPairs(String enterpriseId, String cohort);

    /**
     * 新增精确配对
     *
     * @param dto 配对参数
     * @return 配对结果
     */
    TeacherRelationVO addTeacherPair(TeacherRelationDTO dto);

    /**
     * 编辑精确配对
     *
     * @param relationId 配对ID
     * @param dto        修改参数
     * @return 修改结果
     */
    TeacherRelationVO updateTeacherPair(String relationId, TeacherRelationDTO dto);

    /**
     * 删除精确配对
     *
     * @param relationId 配对ID
     */
    void deleteTeacherPair(String relationId);

    // ==================== 覆盖检查 ====================

    /**
     * 获取企业教师的配对覆盖情况
     *
     * @param enterpriseId 企业ID（可选）
     * @param cohort       届别
     * @return 覆盖情况列表
     */
    List<TeacherCoverageVO> getCoverageList(String enterpriseId, String cohort);

    /**
     * 获取配对覆盖率统计
     *
     * @param cohort 届别
     * @return 统计数据（totalCount, coveredCount, uncoveredCount）
     */
    Map<String, Integer> getCoverageStats(String cohort);

    // ==================== 核心查找逻辑 ====================

    /**
     * 查找企业教师对应的高校教师（优先精确配对，兜底方向级分配）
     *
     * @param enterpriseTeacherId 企业教师 user_id
     * @param directionId        专业方向ID
     * @param cohort             届别
     * @return 高校教师 user_id（null 表示未分配）
     */
    String findUniversityTeacher(String enterpriseTeacherId, String directionId, String cohort);
}

package com.yuwan.completebackend.service;

import com.yuwan.completebackend.model.dto.MajorDTO;
import com.yuwan.completebackend.model.dto.MajorDirectionDTO;
import com.yuwan.completebackend.model.vo.*;

import java.util.List;

/**
 * 专业管理服务接口
 * 提供专业方向和专业的CRUD功能
 *
 * @author 系统架构师
 * @version 1.0
 * @since 2026-03-01
 */
public interface IMajorService {

    // ==================== 专业方向管理 ====================

    /**
     * 获取专业树型结构
     * 返回当前用户企业的专业方向→专业树型结构
     *
     * @param enterpriseId 企业ID（系统管理员可指定，其他角色使用当前用户企业）
     * @param status       状态筛选（可选）
     * @return 树型结构列表
     */
    List<MajorTreeVO> getMajorTree(String enterpriseId, Integer status);

    /**
     * 添加专业方向
     *
     * @param dto 专业方向表单数据
     * @return 专业方向信息
     */
    MajorDirectionVO addDirection(MajorDirectionDTO dto);

    /**
     * 编辑专业方向
     *
     * @param directionId 专业方向ID
     * @param dto         专业方向表单数据
     * @return 专业方向信息
     */
    MajorDirectionVO updateDirection(String directionId, MajorDirectionDTO dto);

    /**
     * 删除专业方向
     * 删除前检查是否有子专业，有则不允许删除
     *
     * @param directionId 专业方向ID
     */
    void deleteDirection(String directionId);

    /**
     * 获取专业方向详情
     *
     * @param directionId 专业方向ID
     * @return 专业方向信息
     */
    MajorDirectionVO getDirectionDetail(String directionId);

    // ==================== 专业管理 ====================

    /**
     * 添加专业
     *
     * @param dto 专业表单数据
     * @return 专业信息
     */
    MajorVO addMajor(MajorDTO dto);

    /**
     * 编辑专业
     *
     * @param majorId 专业ID
     * @param dto     专业表单数据
     * @return 专业信息
     */
    MajorVO updateMajor(String majorId, MajorDTO dto);

    /**
     * 删除专业
     * 删除前检查是否有关联课题，有则不允许删除
     *
     * @param majorId 专业ID
     */
    void deleteMajor(String majorId);

    /**
     * 获取专业详情
     *
     * @param majorId 专业ID
     * @return 专业信息
     */
    MajorVO getMajorDetail(String majorId);

    // ==================== 状态管理 ====================

    /**
     * 切换专业方向状态
     * 禁用时级联禁用所有子专业
     *
     * @param directionId 专业方向ID
     * @param status      目标状态
     */
    void updateDirectionStatus(String directionId, Integer status);

    /**
     * 切换专业状态
     *
     * @param majorId 专业ID
     * @param status  目标状态
     */
    void updateMajorStatus(String majorId, Integer status);

    // ==================== 级联选择器 ====================

    /**
     * 获取级联选择器数据
     * 用于课题创建等场景，返回专业方向→专业的级联数据
     *
     * @param enterpriseId 企业ID（可选，默认使用当前用户企业）
     * @return 级联选择器数据
     */
    List<MajorCascadeVO> getCascadeData(String enterpriseId);

    /**
     * 获取指定企业的所有专业方向（下拉选择用）
     *
     * @param enterpriseId 企业ID
     * @return 专业方向列表
     */
    List<MajorDirectionVO> getDirectionList(String enterpriseId);
}

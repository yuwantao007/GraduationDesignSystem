package com.yuwan.completebackend.service;

import com.yuwan.completebackend.common.PageResult;
import com.yuwan.completebackend.model.dto.CreateSchoolDTO;
import com.yuwan.completebackend.model.dto.UpdateSchoolDTO;
import com.yuwan.completebackend.model.vo.SchoolOptionVO;
import com.yuwan.completebackend.model.vo.SchoolQueryVO;
import com.yuwan.completebackend.model.vo.SchoolVO;

import java.util.List;

/**
 * 学校管理服务接口
 * 定义学校CRUD等业务操作
 *
 * @author 系统架构师
 * @version 1.0
 * @since 2026-02-22
 */
public interface ISchoolService {

    /**
     * 创建学校
     *
     * @param createDTO 创建学校请求参数
     * @return 学校信息VO
     */
    SchoolVO createSchool(CreateSchoolDTO createDTO);

    /**
     * 更新学校信息
     *
     * @param schoolId 学校ID
     * @param updateDTO 更新学校请求参数
     * @return 学校信息VO
     */
    SchoolVO updateSchool(String schoolId, UpdateSchoolDTO updateDTO);

    /**
     * 获取学校详情
     *
     * @param schoolId 学校ID
     * @return 学校信息VO
     */
    SchoolVO getSchoolDetail(String schoolId);

    /**
     * 分页查询学校列表
     *
     * @param queryVO 查询参数
     * @return 分页学校列表
     */
    PageResult<SchoolVO> getSchoolList(SchoolQueryVO queryVO);

    /**
     * 获取全部启用学校（完整版，管理员专用）
     *
     * @return 学校列表
     */
    List<SchoolVO> getAllSchools();

    /**
     * 获取学校下拉选项（精简版，所有登录用户可用）
     * 仅返回 schoolId 和 schoolName
     *
     * @return 学校选项列表
     */
    List<SchoolOptionVO> getSchoolOptions();

    /**
     * 删除学校
     *
     * @param schoolId 学校ID
     */
    void deleteSchool(String schoolId);

    /**
     * 更新学校状态
     *
     * @param schoolId 学校ID
     * @param status 状态(0-禁用, 1-启用)
     */
    void updateSchoolStatus(String schoolId, Integer status);
}

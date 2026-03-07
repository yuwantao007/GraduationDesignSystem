package com.yuwan.completebackend.service;

import com.yuwan.completebackend.common.PageResult;
import com.yuwan.completebackend.model.dto.CreateEnterpriseDTO;
import com.yuwan.completebackend.model.dto.UpdateEnterpriseDTO;
import com.yuwan.completebackend.model.vo.EnterpriseQueryVO;
import com.yuwan.completebackend.model.vo.EnterpriseVO;
import com.yuwan.completebackend.model.vo.UserVO;

import java.util.List;

/**
 * 企业管理服务接口
 * 提供企业CRUD功能
 *
 * @author 系统架构师
 * @version 1.0
 * @since 2026-02-21
 */
public interface IEnterpriseService {

    /**
     * 创建企业
     *
     * @param createDTO 创建企业参数
     * @return 企业信息
     */
    EnterpriseVO createEnterprise(CreateEnterpriseDTO createDTO);

    /**
     * 更新企业信息
     *
     * @param enterpriseId 企业ID
     * @param updateDTO    更新参数
     * @return 更新后的企业信息
     */
    EnterpriseVO updateEnterprise(String enterpriseId, UpdateEnterpriseDTO updateDTO);

    /**
     * 获取企业详情
     *
     * @param enterpriseId 企业ID
     * @return 企业详细信息
     */
    EnterpriseVO getEnterpriseDetail(String enterpriseId);

    /**
     * 分页查询企业列表
     *
     * @param queryVO 查询参数
     * @return 分页结果
     */
    PageResult<EnterpriseVO> getEnterpriseList(EnterpriseQueryVO queryVO);

    /**
     * 获取所有正常状态的企业列表（用于下拉选择）
     *
     * @return 企业列表
     */
    List<EnterpriseVO> getAllEnterprises();

    /**
     * 删除企业
     *
     * @param enterpriseId 企业ID
     */
    void deleteEnterprise(String enterpriseId);

    /**
     * 启用/禁用企业
     *
     * @param enterpriseId 企业ID
     * @param status       状态（0-禁用 1-正常）
     */
    void updateEnterpriseStatus(String enterpriseId, Integer status);

    /**
     * 获取企业概览统计数据
     * 包含企业基本信息及其下属的方向、专业、人员统计
     *
     * @param queryVO 查询参数（支持关键词搜索和状态筛选）
     * @return 企业概览分页结果
     */
    PageResult<com.yuwan.completebackend.model.vo.EnterpriseOverviewVO> getEnterpriseOverview(EnterpriseQueryVO queryVO);

    /**
     * 根据企业名称自动生成企业编码
     * 规则：名称英文首字母简写（汉字转拼音首字母）+ 4位随机数，保证唯一性
     *
     * @param name 企业名称
     * @return 生成的企业编码
     */
    String generateEnterpriseCode(String name);

    /**
     * 搜索企业负责人（ENTERPRISE_LEADER 角色用户）
     *
     * @param keyword 姓名或账号关键词（为空时返回全部）
     * @return 企业负责人列表
     */
    List<UserVO> searchEnterpriseLeaders(String keyword);
}

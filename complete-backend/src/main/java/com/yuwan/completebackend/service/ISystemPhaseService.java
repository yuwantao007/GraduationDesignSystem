package com.yuwan.completebackend.service;

import com.yuwan.completebackend.model.dto.InitPhaseDTO;
import com.yuwan.completebackend.model.dto.SwitchPhaseDTO;
import com.yuwan.completebackend.model.vo.PhaseRecordVO;
import com.yuwan.completebackend.model.vo.PhaseStatusVO;

import java.util.List;

/**
 * 系统阶段管理服务接口
 * 提供阶段查询、阶段切换、阶段校验等功能
 *
 * @author 系统架构师
 * @version 1.0
 * @since 2026-03-07
 */
public interface ISystemPhaseService {

    /**
     * 获取当前阶段状态信息
     * 包含当前阶段详情和全部4个阶段的状态列表
     *
     * @return 阶段状态响应
     */
    PhaseStatusVO getCurrentPhaseStatus();

    /**
     * 初始化系统阶段（首次启动）
     * 将系统设置为"课题申报阶段"，并记录学期信息
     *
     * @param initDTO 初始化参数（包含学期描述）
     * @return 阶段状态响应
     */
    PhaseStatusVO initPhase(InitPhaseDTO initDTO);

    /**
     * 切换到下一阶段
     * 仅管理员可操作，切换后不可回滚
     *
     * @param switchDTO 切换请求参数
     * @return 阶段状态响应
     */
    PhaseStatusVO switchPhase(SwitchPhaseDTO switchDTO);

    /**
     * 查询阶段切换历史记录
     *
     * @return 切换记录列表
     */
    List<PhaseRecordVO> getPhaseRecords();

    /**
     * 检查指定阶段是否为当前激活阶段
     *
     * @param phaseCode 阶段代码
     * @return 是否为当前阶段
     */
    boolean isPhaseActive(String phaseCode);

    /**
     * 获取当前阶段代码
     * 优先从Redis缓存读取，缓存未命中时查库
     *
     * @return 当前阶段代码，未初始化时返回null
     */
    String getCurrentPhaseCode();
}

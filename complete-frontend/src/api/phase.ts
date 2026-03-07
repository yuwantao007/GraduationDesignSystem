/**
 * 系统阶段管理API接口
 * 对应后端SystemPhaseController的接口
 *
 * @author 系统架构师
 * @version 1.0
 * @since 2026-03-07
 */

import request from './request'
import type {
  PhaseStatusVO,
  PhaseRecordVO,
  InitPhaseDTO,
  SwitchPhaseDTO
} from '@/types/phase'

/**
 * 系统阶段管理相关API
 */
export const phaseApi = {
  /**
   * 获取当前系统阶段状态
   * @returns 当前阶段状态（含进度条信息）
   */
  getCurrentPhaseStatus() {
    return request.get<PhaseStatusVO>('/system/phase/current')
  },

  /**
   * 初始化系统阶段
   * @param data 初始化参数（学期、原因）
   * @returns 初始化后的阶段状态
   */
  initPhase(data: InitPhaseDTO) {
    return request.post<PhaseStatusVO>('/system/phase/init', data)
  },

  /**
   * 切换系统阶段
   * @param data 切换参数（目标阶段代码、切换原因）
   * @returns 切换后的阶段状态
   */
  switchPhase(data: SwitchPhaseDTO) {
    return request.post<PhaseStatusVO>('/system/phase/switch', data)
  },

  /**
   * 查询阶段切换历史记录
   * @returns 切换历史记录列表
   */
  getPhaseRecords() {
    return request.get<PhaseRecordVO[]>('/system/phase/records')
  },

  /**
   * 校验指定阶段是否为当前活跃阶段
   * @param phaseCode 阶段代码
   * @returns 是否活跃
   */
  isPhaseActive(phaseCode: string) {
    return request.get<boolean>(`/system/phase/active/${phaseCode}`)
  }
}

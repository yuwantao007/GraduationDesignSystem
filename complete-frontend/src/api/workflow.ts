/**
 * 工作流（Flowable 流程引擎）API 接口
 *
 * @author 系统架构师
 * @version 1.0
 * @since 2026-03-15
 */

import request from './request'
import type { FlowTaskVO, ProcessStatusVO, ProcessInstanceVO, CompleteReviewTaskDTO } from '@/types/workflow'
import type { PageResult } from '@/types/common'

export const workflowApi = {
  /**
   * 获取当前用户的待办任务列表
   */
  getMyTasks(): Promise<FlowTaskVO[]> {
    return request.get<FlowTaskVO[]>('/flow/task/my').then(res => res.data)
  },

  /**
   * 签收任务
   */
  claimTask(taskId: string): Promise<void> {
    return request.post(`/flow/task/${taskId}/claim`).then(() => undefined)
  },

  /**
   * 完成审查任务
   */
  completeReviewTask(taskId: string, data: CompleteReviewTaskDTO): Promise<void> {
    return request.post(`/flow/task/${taskId}/complete`, data).then(() => undefined)
  },

  /**
   * 获取课题当前流程状态
   */
  getProcessStatus(topicId: string): Promise<ProcessStatusVO> {
    return request.get<ProcessStatusVO>(`/flow/process/topic/${topicId}`).then(res => res.data)
  },

  /**
   * 管理员分页查询所有流程实例
   */
  listProcessInstances(params: {
    processStatus?: number
    pageNum?: number
    pageSize?: number
  }): Promise<PageResult<ProcessInstanceVO>> {
    return request.get<PageResult<ProcessInstanceVO>>('/flow/process/list', { params }).then(res => res.data)
  },

  /**
   * 获取课题审查流程定义 BPMN XML（不依赖具体实例，用于流程定义可视化页面）
   */
  getProcessDefinitionXml(): Promise<string> {
    return request.get<string>('/flow/definition/diagram').then(res => res.data)
  },

  /**
   * 获取流程实例的 BPMN XML（前端 bpmn-js 渲染用）
   */
  getProcessDiagram(processInstanceId: string): Promise<string> {
    return request.get<string>(`/flow/process/${processInstanceId}/diagram`).then(res => res.data)
  },

  /**
   * 获取流程实例的历史节点记录
   */
  getProcessHistory(processInstanceId: string): Promise<ProcessStatusVO['historyNodes']> {
    return request.get<ProcessStatusVO['historyNodes']>(`/flow/process/${processInstanceId}/history`).then(res => res.data)
  }
}

package com.yuwan.completebackend.service;

import com.yuwan.completebackend.common.PageResult;
import com.yuwan.completebackend.model.vo.AlertVO;

/**
 * 系统预警服务接口
 * <p>提供预警记录的查询、标记已读、处理以及自动检测功能</p>
 *
 * @author 系统架构师
 * @version 1.0
 * @since 2026-03-14
 */
public interface IAlertService {

    /**
     * 分页查询预警列表
     *
     * @param alertType  预警类型过滤（null=全部）
     * @param alertLevel 预警级别过滤（null=全部）
     * @param isRead     已读状态过滤（null=全部）
     * @param isResolved 处理状态过滤（null=全部）
     * @param page       页码（从1开始）
     * @param size       每页条数
     * @return 分页结果
     */
    PageResult<AlertVO> listAlerts(String alertType, Integer alertLevel,
                                   Integer isRead, Integer isResolved,
                                   int page, int size);

    /**
     * 获取未读预警数量（用于导航栏徽标）
     *
     * @return 未读预警数
     */
    long getUnreadCount();

    /**
     * 标记单条预警为已读
     *
     * @param alertId 预警ID
     */
    void markAsRead(String alertId);

    /**
     * 标记单条预警为已处理
     *
     * @param alertId 预警ID
     */
    void markAsResolved(String alertId);

    /**
     * 全部标记为已读
     */
    void markAllAsRead();

    // ==================== 定时检测方法 ====================

    /**
     * 检查未选报课题的学生（选报阶段触发）
     */
    void checkStudentNotSelected();

    /**
     * 检查无人选报的课题（选报阶段触发）
     */
    void checkTopicNoApplicant();

    /**
     * 检查课题审查积压（待预审课题数量过多时触发）
     */
    void checkReviewBacklog();

    /**
     * 检查阶段截止临近（距截止日期 ≤7 天时触发）
     */
    void checkPhaseDeadline();

    /**
     * 检查整体选报率偏低（选报率 < 60% 时触发）
     */
    void checkSelectionRateLow();
}

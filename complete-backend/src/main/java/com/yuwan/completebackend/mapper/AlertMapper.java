package com.yuwan.completebackend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yuwan.completebackend.model.entity.Alert;
import com.yuwan.completebackend.model.vo.AlertVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 系统预警记录 Mapper 接口
 * <p>单表 CRUD 使用 MyBatis-Plus BaseMapper，复杂查询使用手写 SQL</p>
 *
 * @author 系统架构师
 * @version 1.0
 * @since 2026-03-14
 */
@Mapper
public interface AlertMapper extends BaseMapper<Alert> {

    /**
     * 分页查询预警列表（支持类型/级别/已读/已处理过滤）
     *
     * @param page       分页参数
     * @param alertType  预警类型（null 表示全部）
     * @param alertLevel 预警级别（null 表示全部）
     * @param isRead     是否已读（null 表示全部）
     * @param isResolved 是否已处理（null 表示全部）
     * @return 分页结果
     */
    @Select("<script>" +
            "SELECT alert_id, alert_type, alert_level, alert_title, alert_content, " +
            "       target_id, target_type, is_read, is_resolved, resolved_at, create_time, update_time " +
            "FROM alert_info " +
            "WHERE deleted = 0 " +
            "<if test='alertType != null and alertType != \"\"'> AND alert_type = #{alertType} </if>" +
            "<if test='alertLevel != null'> AND alert_level = #{alertLevel} </if>" +
            "<if test='isRead != null'> AND is_read = #{isRead} </if>" +
            "<if test='isResolved != null'> AND is_resolved = #{isResolved} </if>" +
            "ORDER BY alert_level DESC, create_time DESC" +
            "</script>")
    IPage<AlertVO> selectAlertPage(Page<AlertVO> page,
                                   @Param("alertType") String alertType,
                                   @Param("alertLevel") Integer alertLevel,
                                   @Param("isRead") Integer isRead,
                                   @Param("isResolved") Integer isResolved);

    /**
     * 统计各企业课题数量（用于柱状图）
     * 返回 enterpriseName + count 两列
     */
    @Select("SELECT e.enterprise_name AS enterpriseName, COUNT(t.topic_id) AS count " +
            "FROM enterprise_info e " +
            "LEFT JOIN topic_info t ON e.enterprise_id = t.enterprise_id AND t.deleted = 0 " +
            "WHERE e.deleted = 0 " +
            "GROUP BY e.enterprise_id, e.enterprise_name " +
            "ORDER BY count DESC " +
            "LIMIT 10")
    java.util.List<java.util.Map<String, Object>> selectTopicCountByEnterprise();
}

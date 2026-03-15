package com.yuwan.completebackend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yuwan.completebackend.model.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * 用户信息Mapper接口
 * <p>单表 CRUD 优先使用 MyBatis-Plus BaseMapper + QueryWrapper，
 * 仅保留含数据库函数或多字段原子更新等复杂场景的手写 SQL</p>
 *
 * @author 系统架构师
 * @version 1.0
 * @since 2026-02-20
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {

    /**
     * 更新用户最后登录信息（使用数据库NOW()函数，保证时间一致性）
     *
     * @param userId  用户ID
     * @param loginIp 登录IP
     * @return 更新行数
     */
    @Update("UPDATE user_info SET last_login_time = NOW(), last_login_ip = #{loginIp}, update_time = NOW() WHERE user_id = #{userId}")
    int updateLoginInfo(@Param("userId") String userId, @Param("loginIp") String loginIp);

    /**
     * 统计具有指定角色码的用户数量（用于监控统计）
     *
     * @param roleCode 角色码，如 STUDENT / ENTERPRISE_TEACHER
     * @return 用户数量
     */
    @Select("SELECT COUNT(DISTINCT u.user_id) FROM user_info u " +
            "INNER JOIN user_role ur ON u.user_id = ur.user_id " +
            "INNER JOIN role_info r ON ur.role_id = r.role_id " +
            "WHERE r.role_code = #{roleCode} AND u.deleted = 0")
    long countByRoleCode(@Param("roleCode") String roleCode);
}

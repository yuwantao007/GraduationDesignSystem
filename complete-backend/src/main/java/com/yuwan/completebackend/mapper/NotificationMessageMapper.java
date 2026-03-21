package com.yuwan.completebackend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yuwan.completebackend.model.entity.NotificationMessage;
import org.apache.ibatis.annotations.Mapper;

/**
 * 站内消息Mapper
 *
 * @author 系统架构师
 * @version 1.0
 * @since 2026-03-21
 */
@Mapper
public interface NotificationMessageMapper extends BaseMapper<NotificationMessage> {
}

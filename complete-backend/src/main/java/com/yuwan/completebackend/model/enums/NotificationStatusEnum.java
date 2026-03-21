package com.yuwan.completebackend.model.enums;

import lombok.Getter;

/**
 * 站内消息状态枚举
 *
 * @author 系统架构师
 * @version 1.0
 * @since 2026-03-21
 */
@Getter
public enum NotificationStatusEnum {

    UNREAD(0, "未读"),
    READ(1, "已读"),
    PROCESSED(2, "已处理");

    private final Integer code;
    private final String desc;

    NotificationStatusEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static NotificationStatusEnum fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (NotificationStatusEnum value : values()) {
            if (value.code.equals(code)) {
                return value;
            }
        }
        return null;
    }
}

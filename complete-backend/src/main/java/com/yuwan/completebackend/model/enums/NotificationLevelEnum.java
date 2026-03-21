package com.yuwan.completebackend.model.enums;

import lombok.Getter;

/**
 * 站内消息级别枚举
 *
 * @author 系统架构师
 * @version 1.0
 * @since 2026-03-21
 */
@Getter
public enum NotificationLevelEnum {

    NORMAL(1, "普通通知"),
    REMINDER(2, "一般提醒"),
    IMPORTANT(3, "重要提醒"),
    URGENT(4, "紧急预警");

    private final Integer code;
    private final String desc;

    NotificationLevelEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static NotificationLevelEnum fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (NotificationLevelEnum value : values()) {
            if (value.code.equals(code)) {
                return value;
            }
        }
        return null;
    }
}

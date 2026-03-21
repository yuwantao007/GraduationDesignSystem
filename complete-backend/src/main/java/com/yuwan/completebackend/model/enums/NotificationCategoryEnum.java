package com.yuwan.completebackend.model.enums;

import lombok.Getter;

/**
 * 站内消息分类枚举
 *
 * @author 系统架构师
 * @version 1.0
 * @since 2026-03-21
 */
@Getter
public enum NotificationCategoryEnum {

    REVIEW("REVIEW", "审查通知"),
    DEFENSE("DEFENSE", "答辩通知"),
    GUIDANCE("GUIDANCE", "指导通知"),
    SELECTION("SELECTION", "双选通知"),
    MIDTERM("MIDTERM", "中期通知"),
    SYSTEM("SYSTEM", "系统通知");

    private final String code;
    private final String desc;

    NotificationCategoryEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static NotificationCategoryEnum fromCode(String code) {
        if (code == null) {
            return null;
        }
        for (NotificationCategoryEnum value : values()) {
            if (value.code.equals(code)) {
                return value;
            }
        }
        return null;
    }
}

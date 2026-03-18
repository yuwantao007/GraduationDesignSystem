package com.yuwan.completebackend.common.enums;

import lombok.Getter;

/**
 * 答辩类型枚举
 *
 * @author 系统架构师
 * @version 1.0
 * @since 2026-03-17
 */
@Getter
public enum DefenseType {

    OPENING(1, "开题答辩"),
    MIDTERM(2, "中期答辩"),
    FINAL(3, "正式答辩"),
    SECONDARY(4, "二次答辩");

    private final Integer code;
    private final String desc;

    DefenseType(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static DefenseType getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (DefenseType type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        return null;
    }

    public static String getDescByCode(Integer code) {
        DefenseType type = getByCode(code);
        return type != null ? type.getDesc() : "";
    }
}

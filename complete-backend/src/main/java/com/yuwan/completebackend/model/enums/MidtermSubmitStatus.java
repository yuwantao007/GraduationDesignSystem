package com.yuwan.completebackend.model.enums;

import lombok.Getter;

/**
 * 中期检查提交状态枚举
 *
 * @author 系统架构师
 * @version 1.0
 * @since 2026-03-17
 */
@Getter
public enum MidtermSubmitStatus {

    DRAFT(0, "草稿"),
    SUBMITTED(1, "已提交");

    private final Integer code;
    private final String desc;

    MidtermSubmitStatus(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static MidtermSubmitStatus getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (MidtermSubmitStatus status : values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        return null;
    }

    public static String getDescByCode(Integer code) {
        MidtermSubmitStatus status = getByCode(code);
        return status != null ? status.getDesc() : "";
    }
}

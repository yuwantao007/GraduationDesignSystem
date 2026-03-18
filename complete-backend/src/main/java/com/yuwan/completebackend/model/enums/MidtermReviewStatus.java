package com.yuwan.completebackend.model.enums;

import lombok.Getter;

/**
 * 中期检查审查状态枚举
 *
 * @author 系统架构师
 * @version 1.0
 * @since 2026-03-17
 */
@Getter
public enum MidtermReviewStatus {

    NOT_REVIEWED(0, "未审查"),
    QUALIFIED(1, "合格"),
    UNQUALIFIED(2, "不合格");

    private final Integer code;
    private final String desc;

    MidtermReviewStatus(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static MidtermReviewStatus getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (MidtermReviewStatus status : values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        return null;
    }

    public static String getDescByCode(Integer code) {
        MidtermReviewStatus status = getByCode(code);
        return status != null ? status.getDesc() : "";
    }
}

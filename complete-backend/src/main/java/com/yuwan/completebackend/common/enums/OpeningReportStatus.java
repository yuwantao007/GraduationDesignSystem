package com.yuwan.completebackend.common.enums;

import lombok.Getter;

/**
 * 开题报告审查状态枚举
 *
 * @author 系统架构师
 * @version 1.0
 * @since 2026-03-17
 */
@Getter
public enum OpeningReportStatus {

    NOT_SUBMITTED(0, "未提交"),
    SUBMITTED(1, "已提交待审"),
    PASSED(2, "通过"),
    FAILED(3, "不合格");

    private final Integer code;
    private final String desc;

    OpeningReportStatus(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static OpeningReportStatus getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (OpeningReportStatus status : values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        return null;
    }

    public static String getDescByCode(Integer code) {
        OpeningReportStatus status = getByCode(code);
        return status != null ? status.getDesc() : "";
    }
}

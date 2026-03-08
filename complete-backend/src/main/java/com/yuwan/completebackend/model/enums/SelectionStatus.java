package com.yuwan.completebackend.model.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

/**
 * 课题选报状态枚举
 *
 * @author 系统架构师
 * @version 1.0
 * @since 2026-03-08
 */
@Getter
public enum SelectionStatus {

    /** 待确认 - 学生已选报，等待企业教师确认 */
    PENDING(0, "待确认"),

    /** 中选 - 企业教师已确认选中 */
    SELECTED(1, "中选"),

    /** 落选 - 企业教师未选中 */
    REJECTED(2, "落选");

    @EnumValue
    private final int code;

    @JsonValue
    private final String description;

    SelectionStatus(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public static SelectionStatus fromCode(int code) {
        for (SelectionStatus status : values()) {
            if (status.code == code) {
                return status;
            }
        }
        throw new IllegalArgumentException("未知的选报状态码: " + code);
    }
}

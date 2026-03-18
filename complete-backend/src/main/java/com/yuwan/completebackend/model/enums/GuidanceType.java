package com.yuwan.completebackend.model.enums;

import lombok.Getter;

/**
 * 指导类型枚举
 * <p>
 * 定义指导记录的类型，区分项目指导（企业教师）和论文指导（高校教师）
 * </p>
 *
 * @author 系统架构师
 * @version 1.0
 * @since 2026-03-16
 */
@Getter
public enum GuidanceType {

    /** 项目指导（企业教师） */
    PROJECT(1, "项目指导"),

    /** 论文指导（高校教师） */
    THESIS(2, "论文指导");

    private final Integer code;
    private final String description;

    GuidanceType(Integer code, String description) {
        this.code = code;
        this.description = description;
    }

    /**
     * 根据code值查找枚举
     *
     * @param code 类型代码
     * @return 对应枚举，不存在时返回 null
     */
    public static GuidanceType fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (GuidanceType type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        return null;
    }

    /**
     * 获取类型描述
     *
     * @param code 类型代码
     * @return 类型描述，不存在时返回空字符串
     */
    public static String getDescription(Integer code) {
        GuidanceType type = fromCode(code);
        return type != null ? type.description : "";
    }
}

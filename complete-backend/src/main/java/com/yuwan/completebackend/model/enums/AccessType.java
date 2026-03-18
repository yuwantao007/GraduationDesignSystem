package com.yuwan.completebackend.model.enums;

import lombok.Getter;

/**
 * 文档访问类型枚举
 * <p>
 * 定义文档的访问方式
 * </p>
 *
 * @author 系统架构师
 * @version 1.0
 * @since 2026-03-15
 */
@Getter
public enum AccessType {

    /** 预览 */
    PREVIEW(1, "预览"),

    /** 下载 */
    DOWNLOAD(2, "下载");

    private final Integer code;
    private final String description;

    AccessType(Integer code, String description) {
        this.code = code;
        this.description = description;
    }

    /**
     * 根据code值查找枚举
     *
     * @param code 类型代码
     * @return 对应枚举，不存在时返回 null
     */
    public static AccessType fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (AccessType type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        return null;
    }
}

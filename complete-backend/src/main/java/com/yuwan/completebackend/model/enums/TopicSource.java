package com.yuwan.completebackend.model.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

/**
 * 课题来源枚举
 * 定义课题的来源渠道
 *
 * @author 系统架构师
 * @version 1.0
 * @since 2026-02-21
 */
@Getter
public enum TopicSource {

    /** 校内来源 */
    INTERNAL(1, "校内"),

    /** 校外协同开发 */
    EXTERNAL(2, "校外(协同开发)");

    /**
     * 来源码，存储到数据库
     */
    @EnumValue
    private final int code;

    /**
     * 来源描述
     */
    @JsonValue
    private final String desc;

    TopicSource(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    /**
     * 根据来源码获取枚举
     *
     * @param code 来源码
     * @return 对应的枚举值，未找到返回null
     */
    public static TopicSource fromCode(int code) {
        for (TopicSource source : values()) {
            if (source.code == code) {
                return source;
            }
        }
        return null;
    }
}

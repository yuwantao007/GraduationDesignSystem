package com.yuwan.completebackend.model.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

/**
 * 课题类型枚举
 * 定义毕业设计的类型（设计或论文）
 *
 * @author 系统架构师
 * @version 1.0
 * @since 2026-02-21
 */
@Getter
public enum TopicType {

    /** 设计类型 */
    DESIGN(1, "设计"),

    /** 论文类型 */
    THESIS(2, "论文");

    /**
     * 类型码，存储到数据库
     */
    @EnumValue
    private final int code;

    /**
     * 类型描述
     */
    @JsonValue
    private final String desc;

    TopicType(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    /**
     * 根据类型码获取枚举
     *
     * @param code 类型码
     * @return 对应的枚举值，未找到返回null
     */
    public static TopicType fromCode(int code) {
        for (TopicType type : values()) {
            if (type.code == code) {
                return type;
            }
        }
        return null;
    }
}

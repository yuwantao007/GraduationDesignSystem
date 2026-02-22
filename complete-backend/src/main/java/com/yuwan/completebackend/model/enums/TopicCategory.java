package com.yuwan.completebackend.model.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

/**
 * 课题大类枚举
 * 定义课题的分类类型
 *
 * @author 系统架构师
 * @version 1.0
 * @since 2026-02-21
 */
@Getter
public enum TopicCategory {

    /** 高职升本 */
    UPGRADE(1, "高职升本"),

    /** 3+1联合培养 */
    THREE_PLUS_ONE(2, "3+1"),

    /** 实验班 */
    EXPERIMENTAL(3, "实验班");

    /**
     * 分类码，存储到数据库
     */
    @EnumValue
    private final int code;

    /**
     * 分类描述
     */
    @JsonValue
    private final String desc;

    TopicCategory(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    /**
     * 根据分类码获取枚举
     *
     * @param code 分类码
     * @return 对应的枚举值，未找到返回null
     */
    public static TopicCategory fromCode(int code) {
        for (TopicCategory category : values()) {
            if (category.code == code) {
                return category;
            }
        }
        return null;
    }

    /**
     * 判断是否需要填写适用学校
     * 3+1和实验班需要填写适用学校
     *
     * @return 是否需要填写适用学校
     */
    public boolean requiresApplicableSchool() {
        return this == THREE_PLUS_ONE || this == EXPERIMENTAL;
    }
}

package com.yuwan.completebackend.model.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

/**
 * 课题签名类型枚举
 * 定义课题申报中的签名角色
 *
 * @author 系统架构师
 * @version 1.0
 * @since 2026-02-21
 */
@Getter
public enum TopicSignType {

    /** 分院负责人签名 */
    COLLEGE_LEADER("college_leader", "分院负责人"),

    /** 企业负责人签名 */
    ENTERPRISE_LEADER("enterprise_leader", "企业负责人"),

    /** 企业指导教师签名 */
    ENTERPRISE_TEACHER("enterprise_teacher", "企业指导教师");

    /**
     * 签名类型代码
     */
    @JsonValue
    private final String code;

    /**
     * 签名类型描述
     */
    private final String desc;

    TopicSignType(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    /**
     * 根据代码获取枚举
     *
     * @param code 签名类型代码
     * @return 对应的枚举值，未找到返回null
     */
    public static TopicSignType fromCode(String code) {
        if (code == null) {
            return null;
        }
        for (TopicSignType type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        return null;
    }

    /**
     * 判断签名类型代码是否有效
     *
     * @param code 签名类型代码
     * @return 是否有效
     */
    public static boolean isValid(String code) {
        return fromCode(code) != null;
    }
}

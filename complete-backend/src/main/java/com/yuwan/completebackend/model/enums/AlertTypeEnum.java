package com.yuwan.completebackend.model.enums;

import lombok.Getter;

/**
 * 系统预警类型枚举
 * <p>
 * 定义智能预警系统所有预警场景的类型代码与描述，
 * 对应数据库 alert_info.alert_type 字段存储的枚举字符串值。
 * </p>
 *
 * @author 系统架构师
 * @version 1.0
 * @since 2026-03-14
 */
@Getter
public enum AlertTypeEnum {

    /** 学生未选报任何课题（选报阶段超过阈值天数后触发） */
    STUDENT_NOT_SELECTED("STUDENT_NOT_SELECTED", "学生未选报课题"),

    /** 课题无人选报（选报阶段超过阈值天数后触发） */
    TOPIC_NO_APPLICANT("TOPIC_NO_APPLICANT", "课题无人选报"),

    /** 待审课题积压（待审课题数量超过阈值时触发） */
    REVIEW_BACKLOG("REVIEW_BACKLOG", "课题审查积压"),

    /** 阶段截止临近（距当前阶段截止日期 ≤7 天时触发） */
    PHASE_DEADLINE_NEAR("PHASE_DEADLINE_NEAR", "阶段截止临近"),

    /** 整体选报率偏低（选报率 < 60% 时触发） */
    SELECTION_RATE_LOW("SELECTION_RATE_LOW", "选报率偏低");

    private final String code;
    private final String description;

    AlertTypeEnum(String code, String description) {
        this.code = code;
        this.description = description;
    }

    /**
     * 根据 code 字符串查找枚举
     *
     * @param code 类型代码
     * @return 对应枚举，不存在时返回 null
     */
    public static AlertTypeEnum fromCode(String code) {
        for (AlertTypeEnum type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        return null;
    }
}

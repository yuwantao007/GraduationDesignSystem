package com.yuwan.completebackend.model.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

/**
 * 系统阶段枚举
 * 定义毕业设计全过程管理系统的四个阶段
 *
 * <p>阶段流转说明：</p>
 * <ul>
 *   <li>课题申报阶段：企业教师创建课题、三级审查（预审→初审→终审）</li>
 *   <li>课题双选阶段：学生选报课题、教师确认人选、负责人审查双选</li>
 *   <li>课题指导阶段：项目指导、论文指导、文档提交、开题/中期答辩</li>
 *   <li>毕设答辩阶段：答辩资格审查、正式/二次答辩、成绩评定、文档打印</li>
 * </ul>
 *
 * <p>核心规则：阶段只能按序前进（1→2→3→4），切换后不可回滚</p>
 *
 * @author 系统架构师
 * @version 1.0
 * @since 2026-03-07
 */
@Getter
public enum SystemPhase {

    /**
     * 课题申报阶段 - 课题创建与三级审查
     */
    TOPIC_DECLARATION(1, "课题申报阶段"),

    /**
     * 课题双选阶段 - 学生选报与教师确认
     */
    TOPIC_SELECTION(2, "课题双选阶段"),

    /**
     * 课题指导阶段 - 项目指导与开题/中期答辩
     */
    TOPIC_GUIDANCE(3, "课题指导阶段"),

    /**
     * 毕设答辩阶段 - 正式/二次答辩与成绩评定
     */
    GRADUATION_DEFENSE(4, "毕设答辩阶段");

    /**
     * 阶段序号，存储到数据库
     */
    @EnumValue
    private final int order;

    /**
     * 阶段描述
     */
    @JsonValue
    private final String description;

    SystemPhase(int order, String description) {
        this.order = order;
        this.description = description;
    }

    /**
     * 根据阶段代码获取枚举
     *
     * @param code 阶段代码（如 TOPIC_DECLARATION）
     * @return 对应的枚举值
     * @throws IllegalArgumentException 无效的阶段代码
     */
    public static SystemPhase fromCode(String code) {
        for (SystemPhase phase : values()) {
            if (phase.name().equals(code)) {
                return phase;
            }
        }
        throw new IllegalArgumentException("无效的阶段代码: " + code);
    }

    /**
     * 根据阶段序号获取枚举
     *
     * @param order 阶段序号（1-4）
     * @return 对应的枚举值
     * @throws IllegalArgumentException 无效的阶段序号
     */
    public static SystemPhase fromOrder(int order) {
        for (SystemPhase phase : values()) {
            if (phase.getOrder() == order) {
                return phase;
            }
        }
        throw new IllegalArgumentException("无效的阶段序号: " + order);
    }

    /**
     * 获取下一阶段
     *
     * @return 下一阶段枚举，若已是最后阶段返回null
     */
    public SystemPhase getNextPhase() {
        int nextOrder = this.order + 1;
        for (SystemPhase phase : values()) {
            if (phase.getOrder() == nextOrder) {
                return phase;
            }
        }
        return null;
    }

    /**
     * 判断是否为最后阶段
     *
     * @return 是否为最后阶段
     */
    public boolean isLastPhase() {
        return this == GRADUATION_DEFENSE;
    }

    /**
     * 判断目标阶段是否为当前阶段的下一阶段（严格校验）
     *
     * @param target 目标阶段
     * @return 是否为下一阶段
     */
    public boolean isNextPhase(SystemPhase target) {
        return target != null && target.getOrder() == this.order + 1;
    }
}

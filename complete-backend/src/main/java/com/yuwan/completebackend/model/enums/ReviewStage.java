package com.yuwan.completebackend.model.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

/**
 * 课题审查阶段枚举
 * 定义课题审查流程中的各个审查阶段
 * 
 * <p>审查流程说明：</p>
 * <ul>
 *   <li>高职升本课题：预审(高校教师) → 初审(专业方向主管) → 终审(督导教师)</li>
 *   <li>3+1/实验班课题：初审(专业方向主管) → 终审(高校教师)</li>
 * </ul>
 *
 * @author 系统架构师
 * @version 1.1
 * @since 2026-02-23
 */
@Getter
public enum ReviewStage {

    /**
     * 预审 - 高校教师审查
     * 仅高职升本课题需要此阶段
     */
    PRE_REVIEW(1, "预审", "UNIVERSITY_TEACHER"),

    /**
     * 初审 - 专业方向主管审查
     * 所有课题都需要此阶段
     */
    INIT_REVIEW(2, "初审", "MAJOR_DIRECTOR"),

    /**
     * 终审 - 最终审查
     * <ul>
     *   <li>高职升本课题：由督导教师(SUPERVISOR_TEACHER)执行</li>
     *   <li>3+1/实验班课题：由高校教师(UNIVERSITY_TEACHER)执行</li>
     * </ul>
     */
    FINAL_REVIEW(3, "终审", "SUPERVISOR_TEACHER");

    /**
     * 阶段码，存储到数据库
     */
    @EnumValue
    private final int code;

    /**
     * 阶段描述
     */
    @JsonValue
    private final String desc;

    /**
     * 对应的角色代码
     */
    private final String roleCode;

    ReviewStage(int code, String desc, String roleCode) {
        this.code = code;
        this.desc = desc;
        this.roleCode = roleCode;
    }

    /**
     * 根据阶段码获取枚举
     *
     * @param code 阶段码
     * @return 对应的枚举值，未找到返回null
     */
    public static ReviewStage fromCode(int code) {
        for (ReviewStage stage : values()) {
            if (stage.code == code) {
                return stage;
            }
        }
        return null;
    }

    /**
     * 根据角色代码获取审查阶段
     *
     * @param roleCode 角色代码
     * @return 对应的审查阶段，未找到返回null
     */
    public static ReviewStage fromRoleCode(String roleCode) {
        for (ReviewStage stage : values()) {
            if (stage.roleCode.equals(roleCode)) {
                return stage;
            }
        }
        return null;
    }

    /**
     * 获取下一个审查阶段
     *
     * @return 下一个审查阶段，如果是终审则返回null
     */
    public ReviewStage getNextStage() {
        return switch (this) {
            case PRE_REVIEW -> INIT_REVIEW;
            case INIT_REVIEW -> FINAL_REVIEW;
            case FINAL_REVIEW -> null;
        };
    }

    /**
     * 获取上一个审查阶段
     *
     * @return 上一个审查阶段，如果是预审则返回null
     */
    public ReviewStage getPreviousStage() {
        return switch (this) {
            case PRE_REVIEW -> null;
            case INIT_REVIEW -> PRE_REVIEW;
            case FINAL_REVIEW -> INIT_REVIEW;
        };
    }

    /**
     * 判断当前审查人是否可以修改审查结果
     * 规则：只有下一阶段未通过时才能修改
     *
     * @param currentTopicStatus 当前课题审查状态
     * @return 是否可修改
     */
    public boolean canModifyReview(TopicReviewStatus currentTopicStatus) {
        return switch (this) {
            case PRE_REVIEW -> 
                // 高校教师：专业方向主管未审查之前可修改
                currentTopicStatus == TopicReviewStatus.PRE_PASSED ||
                currentTopicStatus == TopicReviewStatus.PRE_MODIFY;
            case INIT_REVIEW -> 
                // 专业方向主管：督导教师未审查之前可修改
                currentTopicStatus == TopicReviewStatus.INIT_PASSED ||
                currentTopicStatus == TopicReviewStatus.INIT_MODIFY;
            case FINAL_REVIEW -> 
                // 督导教师：终审后可修改审批结果
                currentTopicStatus == TopicReviewStatus.FINAL_PASSED ||
                currentTopicStatus == TopicReviewStatus.FINAL_FAILED;
        };
    }
}

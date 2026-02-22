package com.yuwan.completebackend.model.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

/**
 * 课题审查状态枚举
 * 定义课题申报流程中的各个审查状态
 *
 * @author 系统架构师
 * @version 1.0
 * @since 2026-02-21
 */
@Getter
public enum TopicReviewStatus {

    /** 草稿 - 初始状态，可编辑 */
    DRAFT(0, "草稿"),

    /** 待预审 - 已提交，等待预审 */
    PENDING_PRE(1, "待预审"),

    /** 预审通过 - 预审已通过，等待初审 */
    PRE_PASSED(2, "预审通过"),

    /** 预审需修改 - 预审未通过，需修改后重新提交 */
    PRE_MODIFY(3, "预审需修改"),

    /** 初审通过 - 初审已通过，等待终审 */
    INIT_PASSED(4, "初审通过"),

    /** 初审需修改 - 初审未通过，需修改后重新提交 */
    INIT_MODIFY(5, "初审需修改"),

    /** 终审通过 - 课题审批完成，可进行后续流程 */
    FINAL_PASSED(6, "终审通过"),

    /** 终审不通过 - 课题被拒绝 */
    FINAL_FAILED(7, "终审不通过");

    /**
     * 状态码，存储到数据库
     */
    @EnumValue
    private final int code;

    /**
     * 状态描述
     */
    @JsonValue
    private final String desc;

    TopicReviewStatus(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    /**
     * 根据状态码获取枚举
     *
     * @param code 状态码
     * @return 对应的枚举值，未找到返回null
     */
    public static TopicReviewStatus fromCode(int code) {
        for (TopicReviewStatus status : values()) {
            if (status.code == code) {
                return status;
            }
        }
        return null;
    }

    /**
     * 判断是否为可编辑状态
     * 草稿、预审需修改、初审需修改状态可编辑
     *
     * @return 是否可编辑
     */
    public boolean isEditable() {
        return this == DRAFT || this == PRE_MODIFY || this == INIT_MODIFY;
    }

    /**
     * 判断是否为可提交状态
     * 草稿、预审需修改、初审需修改状态可提交
     *
     * @return 是否可提交
     */
    public boolean isSubmittable() {
        return this == DRAFT || this == PRE_MODIFY || this == INIT_MODIFY;
    }

    /**
     * 判断是否为可撤回状态
     * 仅待预审状态可撤回
     *
     * @return 是否可撤回
     */
    public boolean isWithdrawable() {
        return this == PENDING_PRE;
    }

    /**
     * 判断是否为可删除状态
     * 仅草稿和预审需修改状态可删除
     *
     * @return 是否可删除
     */
    public boolean isDeletable() {
        return this == DRAFT || this == PRE_MODIFY;
    }

    /**
     * 判断是否已通过终审
     *
     * @return 是否通过终审
     */
    public boolean isFinalPassed() {
        return this == FINAL_PASSED;
    }

    /**
     * 判断是否已通过预审（包含后续状态）
     *
     * @return 是否通过预审
     */
    public boolean isAfterPrePassed() {
        return this.code >= PRE_PASSED.code && this != PRE_MODIFY;
    }
}

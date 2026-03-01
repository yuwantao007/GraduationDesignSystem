package com.yuwan.completebackend.model.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

/**
 * 课题审查结果枚举
 * 定义审查人对课题的审查结果
 *
 * @author 系统架构师
 * @version 1.0
 * @since 2026-02-23
 */
@Getter
public enum ReviewResult {

    /**
     * 通过 - 审查通过，进入下一阶段
     */
    PASSED(1, "通过"),

    /**
     * 需修改 - 审查未通过，需修改后重新提交
     */
    NEED_MODIFY(2, "需修改"),

    /**
     * 不通过 - 审查不通过，仅终审可用
     */
    REJECTED(3, "不通过");

    /**
     * 结果码，存储到数据库
     */
    @EnumValue
    private final int code;

    /**
     * 结果描述
     */
    @JsonValue
    private final String desc;

    ReviewResult(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    /**
     * 根据结果码获取枚举
     *
     * @param code 结果码
     * @return 对应的枚举值，未找到返回null
     */
    public static ReviewResult fromCode(int code) {
        for (ReviewResult result : values()) {
            if (result.code == code) {
                return result;
            }
        }
        return null;
    }

    /**
     * 判断审查结果是否可用于指定阶段
     *
     * @param stage 审查阶段
     * @return 是否可用
     */
    public boolean isValidForStage(ReviewStage stage) {
        // 不通过结果仅终审可用
        if (this == REJECTED && stage != ReviewStage.FINAL_REVIEW) {
            return false;
        }
        return true;
    }

    /**
     * 根据审查阶段和审查结果获取对应的课题状态
     *
     * @param stage 审查阶段
     * @return 对应的课题审查状态
     */
    public TopicReviewStatus toTopicStatus(ReviewStage stage) {
        return switch (stage) {
            case PRE_REVIEW -> this == PASSED ? TopicReviewStatus.PRE_PASSED : TopicReviewStatus.PRE_MODIFY;
            case INIT_REVIEW -> this == PASSED ? TopicReviewStatus.INIT_PASSED : TopicReviewStatus.INIT_MODIFY;
            case FINAL_REVIEW -> this == PASSED ? TopicReviewStatus.FINAL_PASSED : TopicReviewStatus.FINAL_FAILED;
        };
    }
}

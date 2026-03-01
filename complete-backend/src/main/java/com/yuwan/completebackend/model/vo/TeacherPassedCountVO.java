package com.yuwan.completebackend.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 教师通过终审课题统计VO
 *
 * @author 系统架构师
 * @version 1.0
 * @since 2026-02-23
 */
@Data
@Schema(description = "教师通过终审课题统计响应")
public class TeacherPassedCountVO {

    @Schema(description = "教师ID")
    private String teacherId;

    @Schema(description = "教师姓名")
    private String teacherName;

    @Schema(description = "已通过终审的课题数量")
    private Integer passedCount;

    @Schema(description = "剩余可提交课题数量")
    private Integer remainingCount;

    @Schema(description = "最大可提交课题数量")
    private Integer maxCount;

    @Schema(description = "是否已达上限")
    private Boolean reachedLimit;

    /**
     * 默认最大课题数量
     */
    public static final int DEFAULT_MAX_COUNT = 18;

    /**
     * 计算剩余可提交课题数
     */
    public void calculateRemaining() {
        if (maxCount == null) {
            maxCount = DEFAULT_MAX_COUNT;
        }
        if (passedCount == null) {
            passedCount = 0;
        }
        this.remainingCount = Math.max(0, maxCount - passedCount);
        this.reachedLimit = passedCount >= maxCount;
    }
}

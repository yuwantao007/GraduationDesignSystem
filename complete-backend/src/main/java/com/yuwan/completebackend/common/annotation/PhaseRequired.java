package com.yuwan.completebackend.common.annotation;

import com.yuwan.completebackend.model.enums.SystemPhase;

import java.lang.annotation.*;

/**
 * 阶段限制注解
 * 标注在Controller方法上，表示该接口只允许在指定阶段被调用
 * 系统管理员不受此限制
 *
 * <p>使用示例：</p>
 * <pre>
 * {@code @PhaseRequired(SystemPhase.TOPIC_DECLARATION)}
 * public Result<TopicVO> createTopic(...) { ... }
 * </pre>
 *
 * @author 系统架构师
 * @version 1.0
 * @since 2026-03-07
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface PhaseRequired {

    /**
     * 允许执行的阶段列表
     * 当前系统阶段必须在此列表中，否则拒绝访问（管理员除外）
     *
     * @return 允许的阶段数组
     */
    SystemPhase[] value();
}

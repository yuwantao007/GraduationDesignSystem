package com.yuwan.completebackend.exception;

import lombok.Getter;

/**
 * 阶段不允许操作异常
 * 当用户在非当前阶段尝试执行受限操作时抛出
 *
 * @author 系统架构师
 * @version 1.0
 * @since 2026-03-07
 */
@Getter
public class PhaseNotAllowedException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * 错误码
     */
    private final Integer code;

    /**
     * 当前系统阶段
     */
    private final String currentPhase;

    /**
     * 此操作要求的阶段
     */
    private final String requiredPhase;

    /**
     * 被拦截的操作名称
     */
    private final String operationName;

    public PhaseNotAllowedException(String currentPhase, String requiredPhase, String operationName) {
        super("当前阶段【" + currentPhase + "】不允许执行操作【" + operationName + "】，该操作需在【" + requiredPhase + "】阶段进行");
        this.code = 403;
        this.currentPhase = currentPhase;
        this.requiredPhase = requiredPhase;
        this.operationName = operationName;
    }
}

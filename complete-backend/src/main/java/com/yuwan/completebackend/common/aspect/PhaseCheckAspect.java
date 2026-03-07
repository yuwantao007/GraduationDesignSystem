package com.yuwan.completebackend.common.aspect;

import com.yuwan.completebackend.common.annotation.PhaseRequired;
import com.yuwan.completebackend.exception.PhaseNotAllowedException;
import com.yuwan.completebackend.model.enums.SystemPhase;
import com.yuwan.completebackend.security.SecurityUtil;
import com.yuwan.completebackend.service.ISystemPhaseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * 阶段校验AOP切面
 * 拦截标注了 @PhaseRequired 的方法，校验当前系统阶段是否允许执行
 * 系统管理员（SYSTEM_ADMIN）不受阶段限制
 *
 * @author 系统架构师
 * @version 1.0
 * @since 2026-03-07
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class PhaseCheckAspect {

    private final ISystemPhaseService systemPhaseService;

    /**
     * 环绕通知：校验当前阶段是否允许执行
     *
     * @param joinPoint       连接点
     * @param phaseRequired   注解实例
     * @return 方法返回值
     * @throws Throwable 方法执行异常
     */
    @Around("@annotation(phaseRequired)")
    public Object checkPhase(ProceedingJoinPoint joinPoint, PhaseRequired phaseRequired) throws Throwable {
        // 系统管理员跳过阶段校验
        if (SecurityUtil.isAdmin()) {
            return joinPoint.proceed();
        }

        // 获取当前系统阶段代码
        String currentPhaseCode = systemPhaseService.getCurrentPhaseCode();

        // 系统未初始化时阻止受限操作
        if (currentPhaseCode == null) {
            String methodName = getMethodName(joinPoint);
            throw new PhaseNotAllowedException("未初始化", getRequiredPhaseDescription(phaseRequired), methodName);
        }

        // 校验当前阶段是否在允许列表中
        SystemPhase[] allowedPhases = phaseRequired.value();
        boolean allowed = Arrays.stream(allowedPhases)
                .anyMatch(phase -> phase.name().equals(currentPhaseCode));

        if (!allowed) {
            String methodName = getMethodName(joinPoint);
            String currentPhaseName = SystemPhase.fromCode(currentPhaseCode).getDescription();
            String requiredPhaseNames = getRequiredPhaseDescription(phaseRequired);
            log.warn("阶段限制拦截: 用户尝试在【{}】阶段执行操作【{}】，需要阶段【{}】",
                    currentPhaseName, methodName, requiredPhaseNames);
            throw new PhaseNotAllowedException(currentPhaseName, requiredPhaseNames, methodName);
        }

        return joinPoint.proceed();
    }

    /**
     * 获取被拦截方法的名称
     */
    private String getMethodName(ProceedingJoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        return signature.getDeclaringType().getSimpleName() + "." + signature.getName();
    }

    /**
     * 获取注解中要求的阶段描述
     */
    private String getRequiredPhaseDescription(PhaseRequired phaseRequired) {
        return Arrays.stream(phaseRequired.value())
                .map(SystemPhase::getDescription)
                .collect(Collectors.joining("、"));
    }
}

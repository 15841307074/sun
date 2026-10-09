package com.htyoudao.youdao.framework.tracer.core.aop;

import com.htyoudao.youdao.framework.tracer.core.annotation.LogExecutionTime;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

/**
 * <p>
 * 注解处理器
 * </p>
 *
 * @author zhangjihe
 * @since 2025-05-11
 */
@Aspect
@Component
@Slf4j
public class LogExecutionTimeAspect {

    @Around("@annotation(logExecutionTime)")
    public Object logExecutionTime(ProceedingJoinPoint joinPoint, LogExecutionTime logExecutionTime) throws Throwable {
        long start = System.currentTimeMillis();
        Object proceed = joinPoint.proceed();
        long executionTime = System.currentTimeMillis() - start;

        log.info("[{}] 执行耗时 {} ms",
                logExecutionTime.value(),
                executionTime);
        return proceed;
    }
}

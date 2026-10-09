package com.htyoudao.youdao.framework.excel.aspect;

import com.htyoudao.youdao.framework.context.BusinessContextHolder;
import com.htyoudao.youdao.framework.excel.core.context.RequestContext;
import com.htyoudao.youdao.framework.excel.core.context.RequestContextHolder;
import com.htyoudao.youdao.framework.security.core.util.SecurityFrameworkUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

/**
 * <p>
 *
 * </p>
 *
 * @author zhangjihe
 * @since 2025-05-27
 */
@Aspect
@Component
public class AsyncContextAspect {

    @Around("execution(* com.htyoudao.youdao.framework.excel.core.service.ExcelActionService.exportAsyncExcel(..))")
    public Object aroundAsyncMethod(ProceedingJoinPoint joinPoint) throws Throwable {
        RequestContext parentContext =
                RequestContext.builder()
                        .businessId(BusinessContextHolder.getBusinessId())
                        .creator(SecurityFrameworkUtils.getLoginUsername())
                        .build();

        return CompletableFuture.runAsync(() -> {
            try {
                if (parentContext != null) {
                    RequestContextHolder.setContext(parentContext);
                }

                try {
                    joinPoint.proceed();
                } catch (Throwable e) {
                    throw new RuntimeException(e);
                }
            } finally {
                RequestContextHolder.clear();
            }
        });
    }
}

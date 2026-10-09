package com.htyoudao.youdao.module.promotion.service.lottery.v2;

import com.htyoudao.youdao.framework.context.BusinessContextHolder;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import jakarta.annotation.Resource;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class LotteryEndpointProtection {
    @Resource
    private LotteryAdmission admission;
    @Resource
    private MeterRegistry metrics;
    @Around("execution(* com.htyoudao.youdao.module.promotion.controller.app.lottery.AppLotteryController.*(..))")
    public Object protect(ProceedingJoinPoint point) throws Throwable {
        String method=point.getSignature().getName();
        Timer.Sample timer=Timer.start(metrics);
        try {
            if("lottery".equals(method)||"result".equals(method))return point.proceed();
            String kind=method.contains("Task")||method.equals("shareCheck")?"task":"query";
            try(var permit=admission.enter(kind,BusinessContextHolder.getRequiredBusinessId(),0,null)){return point.proceed();}
        } catch(Throwable e){metrics.counter("lottery.endpoint.errors","method",method).increment();throw e;}
        finally{timer.stop(Timer.builder("lottery.endpoint.duration").tag("method",method).publishPercentileHistogram().register(metrics));}
    }
}

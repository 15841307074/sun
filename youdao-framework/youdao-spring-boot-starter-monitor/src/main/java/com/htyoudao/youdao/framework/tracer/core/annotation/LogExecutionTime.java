package com.htyoudao.youdao.framework.tracer.core.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @description: 耗时监控
 * @program: cloud-0090
 * @author: zhangjihe
 * @create: 2025-05-11
 **/
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface LogExecutionTime {
    String value() default "";
}

package com.htyoudao.youdao.module.analysis.util.classgenerator;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.Map;

/**
 * @author 给一个类动态添加注解
 */
public class AnnotationUtils {
    
    /**
     * 动态为字段添加注解
     * @param field 目标字段
     * @param annotationClass 注解类型
     * @param annotationValues 注解属性值
     */
    public static void addFieldAnnotation(Field field, 
                                        Class<? extends Annotation> annotationClass,
                                        Map<String, Object> annotationValues) {
        try {
            // 获取注解处理器
            InvocationHandler handler = Proxy.getInvocationHandler(field.getAnnotations()[0]);
            Field memberValuesField = handler.getClass().getDeclaredField("memberValues");
            memberValuesField.setAccessible(true);
            
            // 获取现有注解
            Map<String, Object> memberValues = (Map<String, Object>) memberValuesField.get(handler);
            
            // 创建新注解代理
            Annotation newAnnotation = (Annotation) Proxy.newProxyInstance(
                annotationClass.getClassLoader(),
                new Class[]{annotationClass},
                (proxy, method, args) -> annotationValues.getOrDefault(method.getName(), method.getDefaultValue()));
            
            // 将新注解添加到字段
            memberValues.put(annotationClass.getSimpleName(), newAnnotation);
        } catch (Exception e) {
            throw new RuntimeException("注解添加失败", e);
        }
    }
}
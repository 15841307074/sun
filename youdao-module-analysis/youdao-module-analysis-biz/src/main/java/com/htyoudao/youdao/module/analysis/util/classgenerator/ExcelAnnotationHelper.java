package com.htyoudao.youdao.module.analysis.util.classgenerator;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.converters.Converter;
import com.htyoudao.youdao.module.analysis.enums.OrderExportField;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

/**
 * @author dht
 * 动态添加ExcelProperty注解到指定类的字段
 * 本人闲极无聊搞的
 * 只为一个类加了@ExcelProperty注解
 * 没加@excelProperty注解的字段也会导出(没有忽略注解)
 * 等待有缘人完善  -- dht
 */
public class ExcelAnnotationHelper {

    /**
     * 动态添加ExcelProperty注解到指定类的字段
     */
    public static void addExcelAnnotations(Class<?> targetClass,
                                        List<OrderExportField> selectedFields) {
        try {
            for (OrderExportField exportField : selectedFields) {
                Field field = targetClass.getDeclaredField(exportField.getFieldName());

                // 动态创建注解实例
                ExcelProperty excelProperty = createExcelProperty(exportField.getHeaderName(),exportField.getConverter());

                // 通过反射添加注解
                addAnnotationToField(field, excelProperty);
            }
        } catch (Exception e) {
            throw new RuntimeException("添加Excel注解失败", e);
        }
    }

    private static ExcelProperty createExcelProperty(String headerName,  Class<? extends Converter<?>> converter) {
        return new ExcelProperty() {
            @Override
            public String[] value() {
                return new String[]{headerName};
            }
            @Override
            public int index() {
                return -1; // 自动排序
            }

            @Override
            public int order() {
                return 0;
            }

            @Override
            public Class<? extends Converter<?>> converter() {
                return converter;
            }

            @Override
            public String format() {
                return null;
            }

            @Override
            public Class<? extends Annotation> annotationType() {
                return ExcelProperty.class;
            }
        };
    }

    private static void addAnnotationToField(Field field, Annotation annotation)
            throws Exception {
        Field annotationsField = Field.class.getDeclaredField("annotations");
        annotationsField.setAccessible(true);

        @SuppressWarnings("unchecked")
        Map<Class<? extends Annotation>, Annotation> annotations =
                (Map<Class<? extends Annotation>, Annotation>) annotationsField.get(field);

        annotations.put(annotation.annotationType(), annotation);
    }
}
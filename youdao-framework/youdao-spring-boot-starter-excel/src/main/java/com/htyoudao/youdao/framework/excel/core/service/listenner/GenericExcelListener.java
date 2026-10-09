package com.htyoudao.youdao.framework.excel.core.service.listenner;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.alibaba.excel.read.metadata.holder.ReadRowHolder;
import com.htyoudao.youdao.framework.excel.core.pojo.CellError;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * <p>
 * 监听器
 * </p>
 *
 * @author zhangjihe
 * @since 2025-04-07
 */
@Slf4j
@Getter
public class GenericExcelListener<T> extends AnalysisEventListener<T> {
    private final List<T> successList = new ArrayList<>();
    private final List<String> errors = new ArrayList<>();
    private final Validator validator;

    public GenericExcelListener(Validator validator) {
        this.validator = validator;
    }

    @Override
    public void invoke(T data, AnalysisContext analysisContext) {

        ReadRowHolder holder = analysisContext.readRowHolder();
        Integer rowIndex = holder.getRowIndex();
        if (rowIndex == null || rowIndex < 0) {
            throw new IllegalArgumentException("Invalid row index: " + rowIndex);
        }

        // 验证数据并获取约束违规集合
        Set<ConstraintViolation<T>> violations = validator.validate(data);
        if (violations == null) {
            violations = Collections.emptySet();
        }

        List<CellError> cellErrors = new ArrayList<>();
        if (!violations.isEmpty()) {
            for (ConstraintViolation<T> violation : violations) {
                try {
                    int col = this.getColIndex(violation);
                    if (col < 0) {
                        throw new IllegalArgumentException("Invalid column index: " + col);
                    }
                    cellErrors.add(new CellError(rowIndex, col, violation.getMessage()));
                } catch (Exception e) {
                    log.error("Failed to process violation: {}", violation.getMessage(), e);
                }
            }

            cellErrors.forEach(cellError -> {
                String errorTemplate = "第%s行，第%s列，数据有误，错误信息：%s";
                errors.add(String.format(errorTemplate, cellError.getRowIndex() + 1, cellError.getColIndex() + 1, cellError.getMessage()));
            });
        } else {
            successList.add(data);
        }
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext context) {

    }

    public int getColIndex(ConstraintViolation<?> violation) {
        String propertyPath = violation.getPropertyPath().toString();
        Class<?> beanClass = violation.getRootBeanClass();
        try {
            Field field = beanClass.getDeclaredField(propertyPath);
            ExcelProperty excelProperty = field.getAnnotation(ExcelProperty.class);
            if (excelProperty != null) {
                // 返回注解中的 index 值
                return excelProperty.index();
            }
        } catch (NoSuchFieldException e) {
            // 处理字段未找到的情况（如属性名与字段名不一致）
            e.printStackTrace();
        }
        return -1;
    }


}

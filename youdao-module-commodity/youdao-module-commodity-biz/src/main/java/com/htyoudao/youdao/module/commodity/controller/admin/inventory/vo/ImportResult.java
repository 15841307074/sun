package com.htyoudao.youdao.module.commodity.controller.admin.inventory.vo;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
public class ImportResult<T> {
    private List<T> successList;
    private List<ErrorRow<T>> errorList;
    
    @Data
    @AllArgsConstructor
    public static class ErrorRow<T> {
        private T originalData;
        private String errorMessage;
    }
}
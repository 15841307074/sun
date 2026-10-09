package com.htyoudao.youdao.module.promotion.ExceptionUtil;

public class CouponException extends RuntimeException{

    private String errorCode;

    public CouponException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}

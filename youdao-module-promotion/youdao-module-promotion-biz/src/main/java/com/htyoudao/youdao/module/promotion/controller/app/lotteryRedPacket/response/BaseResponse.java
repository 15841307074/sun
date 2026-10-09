package com.htyoudao.youdao.module.promotion.controller.app.lotteryRedPacket.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 统一响应格式
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
class BaseResponse<T> {
    private Integer code;
    private String message;
    private T data;
    private Long timestamp;

    public static <T> BaseResponse<T> success(T data) {
        return new BaseResponse<>(200, "success", data, System.currentTimeMillis());
    }

    public static <T> BaseResponse<T> error(String message) {
        return new BaseResponse<>(500, message, null, System.currentTimeMillis());
    }
}

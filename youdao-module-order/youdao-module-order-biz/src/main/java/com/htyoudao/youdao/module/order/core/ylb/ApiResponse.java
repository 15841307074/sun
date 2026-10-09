package com.htyoudao.youdao.module.order.core.ylb;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * <p>
 *
 * </p>
 *
 * @author zhangjihe
 * @since 2025-03-26
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse {
    private String sign;
    private String cmd;
    private Integer source;
    private ResponseBody body;
    private String version;
    private Long timestamp;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ResponseBody {
        private Integer code;
        private String errMsg;
    }
}

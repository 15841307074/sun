package com.htyoudao.youdao.module.order.controller.app.sq.VO;

import lombok.Builder;
import lombok.Data;

import java.util.LinkedHashMap;
import java.util.Map;

@Data
@Builder
public class ProcessCodesResp {

    /**
     * code -> 处理结果（成功/跳过/失败原因/下游响应）
     */
    @Builder.Default
    private Map<String, Object> resultMap = new LinkedHashMap<>();
}

package com.htyoudao.youdao.module.analysis.api.enums;

import com.htyoudao.youdao.framework.common.exception.ErrorCode;

/**
 * analysis 系统，使用 1_008_000_000 段
 * @author dht
 */
public interface ErrorCodeConstants {

    ErrorCode SELECT_AT_LEAST_ONE_OPTION = new ErrorCode(1_008_000_001, "请至少选择一个选项");
    ErrorCode ORDER_QUERY_ERROR = new ErrorCode(1_008_000_002, "订单查询失败");
    ErrorCode SCROLL_ORDER_QUERY_ERROR = new ErrorCode(1_008_000_003, "订单滚动查询异常");
}

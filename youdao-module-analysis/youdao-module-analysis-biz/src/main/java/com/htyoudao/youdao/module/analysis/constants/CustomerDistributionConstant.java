package com.htyoudao.youdao.module.analysis.constants;

/**
 * @author dht
 */
public interface CustomerDistributionConstant {

    String RANGE_0 = "0";

    String RANGE_7 = "7";

    String RANGE_14 = "14";

    String RANGE_21 = "21";

    String INTERVAL_0 = "0";

    String INTERVAL_11 = "11";

    String INTERVAL_21 = "21";

    String INTERVAL_31 = "31";

    // 订单数量区间
    /**
     * 1单
     */
    String SINGLE_ORDER = "1";

    /**
     * 2-3单
     */
    String TWO_TO_THREE_ORDERS = "2";

    /**
     * 大于3单
     */
    String ABOVE_THREE_ORDERS = "3";

    // 订单状态
    /**
     * 完结状态 60
     */
    int COMPLETED_STATUS = 60;

    /**
     * 时间入参类型 7天
     */
    int TIME_0 = 0;

    /**
     * 时间入参类型 30天
     */
    int TIME_1 = 1;
}

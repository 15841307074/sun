package com.htyoudao.youdao.module.order.service.job;

public interface JobService {

    void updateCancelOrderTask();

    void cancelTodayWaitingAcceptErrandOrders();

    void completeTodayAcceptedErrandOrders();

    void reportTotalWeek();

    void reportTotalMonth();

    void reportWarehouseWeek();

    void reportWarehouseMonth();

    void reportCommodityWeek();

    void reportCommodityMonth();

    void reportStoreSalesWeek();

    void reportStoreSalesMonth();

    void reportStasticsWeek();

    void reportStasticsMonth();

    void reportAppSalesnumWeek();

    void reportAppSalesnumMonth();

    void reportBuyProportionMonth();

    void reportBuyAmountMonth();
}

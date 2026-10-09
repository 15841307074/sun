package com.htyoudao.youdao.module.analysis.dal.es;


public interface EsField {
    String INDEX = "scm_order_full";

    String STORE_ID = "storeId";
    String STORE_NAME = "storeName";
    String WAREHOUSE_ID = "warehouseId";
    String WAREHOUSE_NAME = "warehouseName";

    String ORDER_STATUS = "orderStatus";
    String ORDER_AMOUNT = "commodityAmount";

    // 统计口径字段（如需改成 parentOrderDate/deliveryDate，就只改这里）
    String ORDER_TIME_FIELD = "orderCreateTime";
}


package com.htyoudao.youdao.module.commodity.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 变动类型
 */
@Getter
@AllArgsConstructor
public enum StockChangeEnum {

    //销售/采购入库/调整/报损/盘点/调拨出库/调拨入库/取消订单返还库存
    SALE("SALE", "销售"),
    PURCHASE_IN("PURCHASE_IN", "采购入库"),
    PURCHASE_OUT("PURCHASE_OUT", "采购退货"),
    PURCHASE_ROLLBACK("PURCHASE_ROLLBACK", "采购回退"),
    LOSS("LOSS", "报损"),
    INVENTORY("INVENTORY", "盘点"),
    TRANSFER_OUT("TRANSFER_OUT", "调拨出库"),
    TRANSFER_IN("TRANSFER_IN", "调拨入库"),
    CANCEL_RETURN("CANCEL_RETURN", "取消订单返还库存"),
    SAN_KUAI_OUT("SAN_KUAI_OUT", "美团出库"),
    ELE_ME_OUT("ELE_ME_OUT", "饿了么出库"),
    SCHOOL_OUT("SCHOOL_OUT", "学校出库"),

    ;

    private final String type;
    private final String description;
}
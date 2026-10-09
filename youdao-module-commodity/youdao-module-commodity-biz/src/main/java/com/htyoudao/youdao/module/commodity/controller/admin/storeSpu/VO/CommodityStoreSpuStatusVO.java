package com.htyoudao.youdao.module.commodity.controller.admin.storeSpu.VO;

import lombok.Data;

@Data
public class CommodityStoreSpuStatusVO {


    private Long commodityStoreSpuId;

    /**
     * 门店下商品的点餐机状态
     */
    private Integer commodityStoreSpuMachineStatus;

    /**
     * 门店下商品的小程序状态
     */
    private Integer commodityStoreSpuAppletStatus;

    /** 1 wx 2dcj*/
    private Integer chooseView;
    /**
     * 门店 id
     */
    private Long storeId;

    /** 小程序上下架状态 1 上架 0 下架*/
    private Integer wxStatus;

    /** 门店上下架状态 1 上架 0 下架*/
    private Integer storeStatus;

}

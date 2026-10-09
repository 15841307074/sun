package com.htyoudao.youdao.module.system.controller.admin.store.vo;

import lombok.Data;

@Data
public class StoreWecomConfigResVO {
    /** 门店id */

    private Long storeId;

    /**
     * 门店名称
     */
    private String storeName;

    /**
     * 门店地址
     */
    private String storeAddress;

    /**
     * 门店经度
     */
    private double longitude;

    /**
     * 门店维度
     */
    private double latitude;

    /**
     * 店铺距离
     */
    private double distance;

    /** 企微二维码 */
    private String qrCode;

}

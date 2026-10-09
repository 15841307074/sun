package com.htyoudao.youdao.module.system.api.org.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * @author dht
 */
@Data
public class StoreOrgDTO implements Serializable {

    private Long storeId;

    private String storeName;
    /**
     * 市
     */
    private String storeCity;

    private String orgName;

    /**
     * 门店状态（0 正常营业 1 闭店）
     */
    private Integer storeStatus;

    /**
     * 营业状态  0 正常营业  1 休息
     */
    private Integer openStatus;


    /**
     * 门店营业时间
     */
    private String  storeHours;


    /**
     * 门店是否支持不付款下单 （0支持 1 不支持）
     */
    private Integer storeWithoutPayment;


}

package com.htyoudao.youdao.module.promotion.api.activity.VO;

import lombok.Data;

import java.io.Serializable;

/**
 * @author dht
 * 优惠券查询使用数据时候的门店数据
 */
@Data
public class StoreInfoRespVO implements Serializable {

    /**
     * 门店id
     */
    private Long storeId;

    /**
     * 门店名称
     */
    private String storeName;

}

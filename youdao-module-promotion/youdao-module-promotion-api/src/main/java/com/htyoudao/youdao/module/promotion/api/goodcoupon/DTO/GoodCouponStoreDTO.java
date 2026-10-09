package com.htyoudao.youdao.module.promotion.api.goodcoupon.DTO;

import lombok.Data;

import java.io.Serializable;

/**
 * 修改优惠券和券包绑定的门店
 * @author dht
 */
@Data
public class GoodCouponStoreDTO implements Serializable {

    private Long storeId;

    private String storeName;
}

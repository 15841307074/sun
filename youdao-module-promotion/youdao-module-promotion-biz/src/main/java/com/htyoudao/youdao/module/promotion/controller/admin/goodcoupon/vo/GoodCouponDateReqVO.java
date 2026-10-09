package com.htyoudao.youdao.module.promotion.controller.admin.goodcoupon.vo;

import lombok.Data;
import com.htyoudao.youdao.framework.common.pojo.PageParam;

import java.util.List;

/**
 * @author dht
 */
@Data
public class GoodCouponDateReqVO extends PageParam{

    private Long couponId;

    private String storeName;

    private List<Long> storeIds;
}

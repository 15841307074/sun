package com.htyoudao.youdao.module.promotion.controller.app.usercoupon.vo;


import lombok.Data;

import java.util.*;

/**
 * @author dht
 */
@Data
public class AppUserCouponRespVO {

    private Set<AppCouponCalculateRespVO> canUseCoupons = new HashSet<>();

    private List<AppCouponCalculateRespVO> result = new ArrayList<>();

    private Set<AppCouponCalculateRespVO> canNotUseCoupons = new HashSet<>();
}

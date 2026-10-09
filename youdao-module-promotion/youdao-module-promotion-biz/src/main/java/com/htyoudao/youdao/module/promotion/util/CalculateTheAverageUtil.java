package com.htyoudao.youdao.module.promotion.util;

import com.htyoudao.youdao.module.promotion.controller.app.usercoupon.vo.AppCouponCalculateRespVO;
import com.htyoudao.youdao.module.promotion.controller.app.usercoupon.vo.AppCouponListRespVO;
import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * @author dht
 */
public class CalculateTheAverageUtil {

    public static void calculateTheAverage(AppCouponCalculateRespVO userCoupon, int size, BigDecimal reduceAmount) {
        userCoupon.setGoodSize(size);
        userCoupon.setMoney(reduceAmount.divide(BigDecimal.valueOf(size), 2, RoundingMode.HALF_UP));
    }
}

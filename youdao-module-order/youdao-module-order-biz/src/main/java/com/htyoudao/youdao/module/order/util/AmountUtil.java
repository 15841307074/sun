package com.htyoudao.youdao.module.order.util;

import cn.hutool.core.lang.Assert;
import com.htyoudao.youdao.module.order.core.submit.DTO.DiscountResultDTO;

import java.math.BigDecimal;

public class AmountUtil {

    public static int yuan2Fen(BigDecimal yuan) {
        int value = 0;

        try {
            BigDecimal var2 = new BigDecimal(100);
            BigDecimal var3 = yuan.multiply(var2);
            value = Integer.parseInt(var3.stripTrailingZeros().toPlainString());
        } catch (Exception e) {
            throw new IllegalArgumentException(String.format("非法金额[%s]", yuan));
        }

        Assert.isTrue(value >= 0, String.format("非法金额[%s]", yuan));
        return value;
    }

    public static double fen2Yuan(BigDecimal yuan) {
        double value = 0;

        try {
            BigDecimal var2 = new BigDecimal(100);
            BigDecimal var3 = yuan.divide(var2, 2, BigDecimal.ROUND_HALF_UP);
           value = var3.doubleValue();
        } catch (Exception e) {
            throw new IllegalArgumentException(String.format("非法金额[%s]", yuan));
        }

        Assert.isTrue(value >= 0, String.format("非法金额[%s]", yuan));
        return value;
    }

    /**
     * 计算优惠金额
     *
     * @param orderAmount
     * @param discountRule
     * @return
     */
    public static DiscountResultDTO calculateDiscount(BigDecimal orderAmount, String discountRule) {
        //解析满减规则
        String[] ruleParts = discountRule.split("#");
        //满减门槛
        BigDecimal threshold = new BigDecimal(ruleParts[0]);
        //减免金额
        BigDecimal discount = new BigDecimal(ruleParts[1]);

        //计算减免金额
        BigDecimal discountAmount = BigDecimal.ZERO;
        if (orderAmount.compareTo(threshold) >= 0) {
            //满足条件，减免金额
            discountAmount = discount;
        }

        //计算折后金额
        BigDecimal finalAmount = orderAmount.subtract(discountAmount);

        return new DiscountResultDTO(discountAmount, finalAmount);
    }
}

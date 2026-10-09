package com.htyoudao.youdao.module.promotion.service.usercoupon.calculatediscount.impl;

import com.htyoudao.youdao.module.promotion.controller.app.usercoupon.vo.AppCouponCalculateRespVO;
import com.htyoudao.youdao.module.promotion.controller.app.usercoupon.vo.AppCouponListRespVO;
import com.htyoudao.youdao.module.promotion.controller.app.usercoupon.vo.OrderGoods;
import com.htyoudao.youdao.module.promotion.controller.app.usercoupon.vo.SettlementReqVO;
import com.htyoudao.youdao.module.promotion.service.usercoupon.calculatediscount.CalculateDiscount;
import com.htyoudao.youdao.module.promotion.util.CalculateTheAverageUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * 折扣通用券
 * @author dht
 */
@Service("discountCommon")
@Slf4j
public class DiscountCommonImpl implements CalculateDiscount {


    @Override
    public AppCouponCalculateRespVO calculateDiscount(AppCouponCalculateRespVO userCoupon, SettlementReqVO settlementReqVO) {
        log.info("进入通用折扣券DiscountCommonImpl");
        List<OrderGoods> goodsList = settlementReqVO.getGoodsList();
        List<Long> list = goodsList.stream().map(OrderGoods::getCommodityId).toList();
        BigDecimal transactionAmount = settlementReqVO.getTransactionAmount();
        String discount = userCoupon.getReliefOrDiscount().toPlainString();
        BigDecimal ten = new BigDecimal(10);
        BigDecimal dis = new BigDecimal(discount);
        BigDecimal subtract = ten.subtract(dis);
        BigDecimal res = transactionAmount.multiply(subtract).divide(ten).setScale(2, RoundingMode.HALF_UP);
        userCoupon.setReduceAmount(res);
        CalculateTheAverageUtil.calculateTheAverage(userCoupon,settlementReqVO.getGoodSize(),res);
        userCoupon.setCommodityIds(list);
        return userCoupon;
    }
}
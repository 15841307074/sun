package com.htyoudao.youdao.module.promotion.service.usercoupon.calculatediscount.impl;

import com.htyoudao.youdao.module.promotion.context.CommodityIdsContext;
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
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 折扣指定商品不可用优惠券
 * @author dht
 */
@Service("discountNotCommon")
@Slf4j
public class DiscountNotCommonImpl implements CalculateDiscount {


    @Override
    public AppCouponCalculateRespVO calculateDiscount(AppCouponCalculateRespVO userCoupon, SettlementReqVO settlementReqVO) {
        log.info("进入折扣指定商品不可用优惠券DiscountNotCommonImpl");
        BigDecimal transactionAmount;
        String discount = userCoupon.getReliefOrDiscount().toPlainString();
        List<Long> commodityIds2 = CommodityIdsContext.getCommodityId2().get(userCoupon.getCouponId());

        //参与优惠的商品
        List<OrderGoods> goodsList = settlementReqVO.getGoodsList();
        List<OrderGoods> transList = new ArrayList<>();
        List<Long> commodityIds = new ArrayList<>();
        for (OrderGoods goods : goodsList) {
            if (!commodityIds2.contains(goods.getCommodityId())) {
                transList.add(goods);
                commodityIds.add(goods.getCommodityId());
            }
        }
        //价格从大到小排序
        transList.stream()
                .sorted((a, b) -> b.getTransactionAmount().compareTo(a.getTransactionAmount()))
                .collect(Collectors.toList());
        // 获取参与优惠的最贵商品
        OrderGoods orderGoods = transList.get(0);
        transactionAmount = orderGoods.getTransactionAmount();
        //计算折扣金额
        BigDecimal ten = new BigDecimal(10);
        BigDecimal dis = new BigDecimal(discount);
        BigDecimal subtract = ten.subtract(dis);
        BigDecimal res = transactionAmount.multiply(subtract).divide(ten, 2, RoundingMode.HALF_UP);
        userCoupon.setReduceAmount(res);
        CalculateTheAverageUtil.calculateTheAverage(userCoupon,transList.size(),res);
        userCoupon.setCommodityIds(commodityIds);
        return userCoupon;
    }
}
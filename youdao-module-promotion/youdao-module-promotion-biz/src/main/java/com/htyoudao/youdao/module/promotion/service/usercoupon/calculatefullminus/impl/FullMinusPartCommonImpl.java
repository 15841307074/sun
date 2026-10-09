package com.htyoudao.youdao.module.promotion.service.usercoupon.calculatefullminus.impl;

import com.htyoudao.youdao.module.promotion.context.CommodityIdsContext;
import com.htyoudao.youdao.module.promotion.controller.app.usercoupon.vo.AppCouponCalculateRespVO;
import com.htyoudao.youdao.module.promotion.controller.app.usercoupon.vo.AppCouponListRespVO;
import com.htyoudao.youdao.module.promotion.controller.app.usercoupon.vo.OrderGoods;
import com.htyoudao.youdao.module.promotion.controller.app.usercoupon.vo.SettlementReqVO;

import com.htyoudao.youdao.module.promotion.service.usercoupon.calculatefullminus.CalculateFullMinus;
import com.htyoudao.youdao.module.promotion.util.CalculateTheAverageUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;


/**
 * 满减指定商品可用优惠券
 * @author  dht
 */
@Service("fullMinusPartCommon")
@Slf4j
public class FullMinusPartCommonImpl implements CalculateFullMinus {


    @Override
    public AppCouponCalculateRespVO calculateDiscount(AppCouponCalculateRespVO userCoupon, SettlementReqVO settlementReqVO) {
        log.info("进入满减指定商品可用优惠券FullMinusPartCommonImpl");
        BigDecimal reduceAmount = userCoupon.getReliefOrDiscount();
        List<Long> commodityId1 = CommodityIdsContext.getCommodityId1().get(userCoupon.getCouponId());

        //参与优惠的商品
        List<OrderGoods> goodsList = settlementReqVO.getGoodsList();
        List<OrderGoods> transList = new ArrayList<>();
        List<Long> commodityIds = new ArrayList<>();
        for (OrderGoods goods : goodsList) {
            if (commodityId1.contains(goods.getCommodityId())) {
                transList.add(goods);
                commodityIds.add(goods.getCommodityId());
            }
        }
        //价格从大到小排序
        BigDecimal partTransactionAmount = transList.stream()
                .map(OrderGoods::getTransactionAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 算钱
        if (partTransactionAmount.compareTo(reduceAmount) <= 0 ) {
            userCoupon.setReduceAmount(partTransactionAmount);
            userCoupon.setCommodityIds(commodityIds);
            CalculateTheAverageUtil.calculateTheAverage(userCoupon,transList.size(),partTransactionAmount);
        } else {
            CalculateTheAverageUtil.calculateTheAverage(userCoupon,transList.size(),reduceAmount);
            userCoupon.setReduceAmount(reduceAmount);
            userCoupon.setCommodityIds(commodityIds);
        }
        return userCoupon;
    }
}
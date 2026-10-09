package com.htyoudao.youdao.module.promotion.service.usercoupon.calculatefullminus.impl;

import com.htyoudao.youdao.module.promotion.controller.app.usercoupon.vo.AppCouponCalculateRespVO;
import com.htyoudao.youdao.module.promotion.controller.app.usercoupon.vo.AppCouponListRespVO;
import com.htyoudao.youdao.module.promotion.controller.app.usercoupon.vo.OrderGoods;
import com.htyoudao.youdao.module.promotion.controller.app.usercoupon.vo.SettlementReqVO;
import com.htyoudao.youdao.module.promotion.service.usercoupon.calculatefullminus.CalculateFullMinus;
import com.htyoudao.youdao.module.promotion.util.CalculateTheAverageUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

/**
 * 满减通用优惠券
 * @author dht
 */
@Slf4j
@Service("fullMinusCommon")
public class FullMinusCommonImpl implements CalculateFullMinus {


    @Override
    public AppCouponCalculateRespVO calculateDiscount(AppCouponCalculateRespVO userCoupon, SettlementReqVO settlementReqVO) {
        log.info("满减通用优惠券FullMinusCommonImpl");
        BigDecimal transactionAmount = settlementReqVO.getTransactionAmount();
        BigDecimal reduceAmount = userCoupon.getReliefOrDiscount();
        List<OrderGoods> goodsList = settlementReqVO.getGoodsList();
        List<Long> list = goodsList.stream().map(OrderGoods::getCommodityId).toList();
        if (transactionAmount.compareTo(reduceAmount) <= 0) {
            userCoupon.setReduceAmount(transactionAmount);
            userCoupon.setRemark("0元购");
            userCoupon.setCommodityIds(list);
            CalculateTheAverageUtil.calculateTheAverage(userCoupon,settlementReqVO.getGoodSize(),transactionAmount);
            return userCoupon;
        } else {
            userCoupon.setReduceAmount(reduceAmount);
            userCoupon.setCommodityIds(list);
            CalculateTheAverageUtil.calculateTheAverage(userCoupon,settlementReqVO.getGoodSize(),reduceAmount);
            return userCoupon;
        }
    }
}

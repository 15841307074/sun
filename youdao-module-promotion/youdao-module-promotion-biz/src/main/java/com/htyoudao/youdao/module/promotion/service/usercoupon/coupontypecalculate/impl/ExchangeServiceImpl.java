package com.htyoudao.youdao.module.promotion.service.usercoupon.coupontypecalculate.impl;


import com.htyoudao.youdao.module.promotion.constant.UserCouponConstants;
import com.htyoudao.youdao.module.promotion.context.CommodityIdsContext;
import com.htyoudao.youdao.module.promotion.controller.app.usercoupon.vo.AppCouponCalculateRespVO;
import com.htyoudao.youdao.module.promotion.controller.app.usercoupon.vo.AppCouponListRespVO;
import com.htyoudao.youdao.module.promotion.controller.app.usercoupon.vo.OrderGoods;
import com.htyoudao.youdao.module.promotion.controller.app.usercoupon.vo.SettlementReqVO;
import com.htyoudao.youdao.module.promotion.service.usercoupon.coupontypecalculate.CouponTypeCalculate;
import com.htyoudao.youdao.module.promotion.util.CalculateTheAverageUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


/**
 * 计算商品兑换券
 * @author dht
 */
@Slf4j
@Service("exchange")
public class ExchangeServiceImpl implements CouponTypeCalculate {

    @Override
    public AppCouponCalculateRespVO calculateDiscount(AppCouponCalculateRespVO userCoupon, SettlementReqVO settlementReqVO) {
        log.info("进入折扣指定商品可用券ExchangeServiceImpl");
        Integer fullReduction = userCoupon.getFullReduction();
        // 需要满足满减的兑换券无需计算 因为过滤时候算完了
        if(Objects.equals(fullReduction, UserCouponConstants.DOOR_SILL_TYPE_1)){
            return userCoupon;
        }
        BigDecimal transactionAmount;
        List<Long> commodityIds1 = CommodityIdsContext.getCommodityId1().get(userCoupon.getCouponId());

        //参与优惠的商品
        List<OrderGoods> goodsList = settlementReqVO.getGoodsList();
        List<OrderGoods> transList = new ArrayList<>();
        for (OrderGoods goods : goodsList) {
            if (commodityIds1.contains(goods.getCommodityId())) {
                transList.add(goods);
                break;
            }
        }

        // 获取参与优惠的最贵商品
        OrderGoods orderGoods = transList.get(0);
        if(Objects.equals(userCoupon.getDoorsillType(), UserCouponConstants.DOOR_SILL_TYPE_3)){
            transactionAmount = orderGoods.getTransactionAmount().setScale(2, RoundingMode.HALF_UP);
            //计算折扣金额
            userCoupon.setReduceAmount(transactionAmount);
            userCoupon.getCommodityIds().add(orderGoods.getCommodityId());
            CalculateTheAverageUtil.calculateTheAverage(userCoupon,1,transactionAmount);
        }

        if(Objects.equals(userCoupon.getDoorsillType(), UserCouponConstants.DOOR_SILL_TYPE_4)){
            BigDecimal transactionAmount1 = orderGoods.getTransactionAmount();
            BigDecimal reliefOrDiscount = userCoupon.getReliefOrDiscount();
            transactionAmount = transactionAmount1.subtract(reliefOrDiscount).setScale(2, RoundingMode.HALF_UP);
            //计算折扣金额
            userCoupon.setReduceAmount(transactionAmount);
            userCoupon.getCommodityIds().add(orderGoods.getCommodityId());
            CalculateTheAverageUtil.calculateTheAverage(userCoupon,1,transactionAmount);
        }

        return userCoupon;
    }
}

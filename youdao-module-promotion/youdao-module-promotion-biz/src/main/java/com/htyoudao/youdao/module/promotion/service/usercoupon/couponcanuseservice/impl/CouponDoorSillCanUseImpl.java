package com.htyoudao.youdao.module.promotion.service.usercoupon.couponcanuseservice.impl;


import cn.hutool.core.util.ObjectUtil;
import com.htyoudao.youdao.module.promotion.constant.GoodCouponConstants;
import com.htyoudao.youdao.module.promotion.constant.UserCouponConstants;
import com.htyoudao.youdao.module.promotion.context.CommodityIdsContext;
import com.htyoudao.youdao.module.promotion.context.UserCouponCommodityContext;
import com.htyoudao.youdao.module.promotion.controller.app.usercoupon.vo.*;
import com.htyoudao.youdao.module.promotion.dal.dataobject.usercoupon.UserCouponDO;
import com.htyoudao.youdao.module.promotion.service.usercoupon.couponcanuseservice.CouponCanUseService;
import com.htyoudao.youdao.module.promotion.util.CalculateTheAverageUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author dht
 */
@Slf4j
@Service("couponDoorSillCanUse")
public class CouponDoorSillCanUseImpl implements CouponCanUseService {


    /**
     * 判断优惠券适用门槛
     * @param settlementReqVO settlementReqVO
     * @param userCouponList userCouponList
     * @return UserCouponDO
     */
    @Override
    public AppUserCouponRespVO isDetermineIfItCanBeUsed(SettlementReqVO settlementReqVO, Set<AppCouponCalculateRespVO> userCouponList, AppUserCouponRespVO appUserCouponRespVO) {
        log.info("优惠券门槛判断");
        List<OrderGoods> goodsList = settlementReqVO.getGoodsList();
        //Set<Long> skuIds = goodsList.stream().map(OrderGoods::getCommodityId).collect(Collectors.toSet());
        //Map<Long, Collection<Long>> canUseCommodities = UserCouponCommodityContext.getCanUseCommodities();

        Set<AppCouponCalculateRespVO> matchingList = new HashSet<>();

        for (AppCouponCalculateRespVO appCouponListRespVO : userCouponList) {
            Integer couponType = appCouponListRespVO.getCouponType();
            Integer doorsillType = appCouponListRespVO.getDoorsillType();
            if(Objects.equals(couponType, GoodCouponConstants.COUPON_TYPE_5)){
                // 商品券的数量门槛
                if(Objects.equals(doorsillType, UserCouponConstants.DOOR_SILL_TYPE_5) || Objects.equals(doorsillType, UserCouponConstants.DOOR_SILL_TYPE_6)){
                    exchangedCalculateCommodity(appCouponListRespVO,settlementReqVO,matchingList,appUserCouponRespVO);
                }
                continue;
            }


            //无门槛券
            if(Objects.equals(doorsillType, UserCouponConstants.DOOR_SILL_TYPE_0)){
                matchingList.add(appCouponListRespVO);
            }

            // 价格门槛
            if(Objects.equals(doorsillType, UserCouponConstants.DOOR_SILL_TYPE_1)){
                BigDecimal doorsill = appCouponListRespVO.getDoorsill();
                //  指定商品
                if(appCouponListRespVO.getIsCommonStore() != 1){
                    // 价格门槛券  价格大于门槛 不指定商品
                    if(doorsill.compareTo(settlementReqVO.getTransactionAmount()) <= 0){
                        matchingList.add(appCouponListRespVO);
                    }else {
                        appCouponListRespVO.setRemark("金额差" + doorsill.subtract(settlementReqVO.getTransactionAmount()) + "元可用");
                        appUserCouponRespVO.getCanNotUseCoupons().add(appCouponListRespVO);
                    }
                }else {
                    // 价格门槛券  价格大于门槛 不指定商品
                    if(doorsill.compareTo(settlementReqVO.getTransactionAmount()) <= 0){
                        matchingList.add(appCouponListRespVO);
                    }else {
                        appCouponListRespVO.setRemark("金额差" + doorsill.subtract(settlementReqVO.getTransactionAmount()) + "元可用");
                        appUserCouponRespVO.getCanNotUseCoupons().add(appCouponListRespVO);
                    }
                }
            }



            // 数量门槛
            if(Objects.equals(doorsillType, UserCouponConstants.DOOR_SILL_TYPE_2)){
                // 数量门槛的数量
                BigDecimal doorsill = appCouponListRespVO.getDoorsill();
                // 商品数量
                int size = goodsList.size();
                // 指定了可用商品
                if(appCouponListRespVO.getIsCommonStore() != 1){
                    if(doorsill.compareTo(BigDecimal.valueOf(size)) <= 0){
                        matchingList.add(appCouponListRespVO);
                    }else {
                        int i = doorsill.intValue() - size;
                        appCouponListRespVO.setRemark("可用商品差" + i + "件可用");
                        appUserCouponRespVO.getCanNotUseCoupons().add(appCouponListRespVO);
                    }
                }else {
                    if(doorsill.compareTo(BigDecimal.valueOf(size)) <= 0){
                        matchingList.add(appCouponListRespVO);
                    }else {
                        int i = doorsill.intValue() - size;
                        appCouponListRespVO.setRemark("商品差" + i + "件可用");
                        appUserCouponRespVO.getCanNotUseCoupons().add(appCouponListRespVO);
                    }
                }
            }
            // 兑换券的付款门槛
            if(Objects.equals(doorsillType, UserCouponConstants.DOOR_SILL_TYPE_3) || Objects.equals(doorsillType, UserCouponConstants.DOOR_SILL_TYPE_4)){
                exchangedCalculateExchange(appCouponListRespVO,settlementReqVO,matchingList,appUserCouponRespVO);
            }
        }
        appUserCouponRespVO.setCanUseCoupons(matchingList);
        return appUserCouponRespVO;

//        userCouponList.stream().filter(item ->
//                //无门槛券
//                item.getDoorsillType() == 0
//                // 价格门槛券  价格大于门槛
//            || (item.getDoorsillType() == 1 && settlementReqVO.getTransactionAmount().compareTo(item.getDoorsill()) >= 0)
//                // 商品数量门槛券  商品数量大于门槛
//            || (item.getDoorsillType() == 2 && item.getDoorsill().compareTo(BigDecimal.valueOf(goodsList.size())) <= 0)
//            || (item.getDoorsillType() == 3)
//            || (item.getDoorsillType() == 4)
//        ).toList();
    }

    /**
     * 兑换券的提前计算
     */
    private void exchangedCalculateExchange(AppCouponCalculateRespVO appCouponListRespVO,SettlementReqVO settlementReqVO,Set<AppCouponCalculateRespVO> matchingList,AppUserCouponRespVO appUserCouponRespVO){
        Integer fullReduction = appCouponListRespVO.getFullReduction();
//        if(!Objects.equals(fullReduction,1)){
//            matchingList.add(appCouponListRespVO);
//            return;
//        }

        BigDecimal totalAmount = settlementReqVO.getTransactionAmount();

        log.info("提前进入折扣指定商品可用券ExchangeServiceImpl");
        BigDecimal transactionAmount;
        List<Long> commodityIds1 = CommodityIdsContext.getCommodityId1().get(appCouponListRespVO.getCouponId());

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
        if(Objects.equals(appCouponListRespVO.getDoorsillType(), UserCouponConstants.DOOR_SILL_TYPE_3)){
            transactionAmount = orderGoods.getTransactionAmount().setScale(2, RoundingMode.HALF_UP);
            BigDecimal subtract = totalAmount.subtract(transactionAmount);
            if(Objects.equals(fullReduction,1)){
                if(subtract.compareTo(appCouponListRespVO.getDoorsill()) >= 0){
                    //计算折扣金额
                    appCouponListRespVO.setReduceAmount(transactionAmount);
                    appCouponListRespVO.getCommodityIds().add(orderGoods.getCommodityId());
                    CalculateTheAverageUtil.calculateTheAverage(appCouponListRespVO,1,transactionAmount);
                    matchingList.add(appCouponListRespVO);
                }else {
                    appCouponListRespVO.setRemark("订单金额差" + appCouponListRespVO.getDoorsill().subtract(subtract) + "元可用");
                    appUserCouponRespVO.getCanNotUseCoupons().add(appCouponListRespVO);
                }
            }else {
                //计算折扣金额
                appCouponListRespVO.setReduceAmount(transactionAmount);
                appCouponListRespVO.getCommodityIds().add(orderGoods.getCommodityId());
                CalculateTheAverageUtil.calculateTheAverage(appCouponListRespVO,1,transactionAmount);
                matchingList.add(appCouponListRespVO);
            }

        }

        if(Objects.equals(appCouponListRespVO.getDoorsillType(), UserCouponConstants.DOOR_SILL_TYPE_4)){
            BigDecimal transactionAmount1 = orderGoods.getTransactionAmount();
            BigDecimal reliefOrDiscount = appCouponListRespVO.getReliefOrDiscount();
            if(reliefOrDiscount.compareTo(transactionAmount1) > 0){
                appCouponListRespVO.setRemark("用券后实付金额高于原价，该券暂不可用");
                appUserCouponRespVO.getCanNotUseCoupons().add(appCouponListRespVO);
                return;
            }
            BigDecimal add = totalAmount.subtract(transactionAmount1).add(reliefOrDiscount);
            if(Objects.equals(fullReduction,1)){
                if(add.compareTo(appCouponListRespVO.getDoorsill()) >= 0){
                    transactionAmount = transactionAmount1.subtract(reliefOrDiscount).setScale(2, RoundingMode.HALF_UP);
                    //计算折扣金额
                    appCouponListRespVO.setReduceAmount(transactionAmount);
                    appCouponListRespVO.getCommodityIds().add(orderGoods.getCommodityId());
                    CalculateTheAverageUtil.calculateTheAverage(appCouponListRespVO,1,transactionAmount);
                    matchingList.add(appCouponListRespVO);
                }else {
                    appCouponListRespVO.setRemark("订单金额差" + appCouponListRespVO.getDoorsill().subtract(add) + "元可用");
                    appUserCouponRespVO.getCanNotUseCoupons().add(appCouponListRespVO);
                }
            }else {
                transactionAmount = transactionAmount1.subtract(reliefOrDiscount).setScale(2, RoundingMode.HALF_UP);
                //计算折扣金额
                appCouponListRespVO.setReduceAmount(transactionAmount);
                appCouponListRespVO.getCommodityIds().add(orderGoods.getCommodityId());
                CalculateTheAverageUtil.calculateTheAverage(appCouponListRespVO,1,transactionAmount);
                matchingList.add(appCouponListRespVO);
            }
        }
    }


    /**
     * 商品券的提前计算
     */
    private void exchangedCalculateCommodity(AppCouponCalculateRespVO appCouponListRespVO,SettlementReqVO settlementReqVO,Set<AppCouponCalculateRespVO> matchingList,AppUserCouponRespVO appUserCouponRespVO){
        Integer fullReduction = appCouponListRespVO.getFullReduction();
        Integer doorsillType = appCouponListRespVO.getDoorsillType();
        String singleIds = appCouponListRespVO.getSingleIds();
        int discount = Integer.parseInt(appCouponListRespVO.getDiscount());
        BigDecimal reduceAmount = appCouponListRespVO.getReduceAmount();
        long singleId = Long.parseLong(singleIds);
        //List<OrderGoods> goodsList = settlementReqVO.getGoodsList().stream().filter(item -> item.getCommodityId().equals(singleId) && ObjectUtil.isEmpty(item.getActivityId())).toList();
        List<OrderGoods> goodsList = settlementReqVO.getGoodsList().stream()
                .filter(item -> item.getCommodityId().equals(singleId) && ObjectUtil.isEmpty(item.getActivityId()))
                .sorted(Comparator.comparing(
                        OrderGoods::getTransactionAmount,
                        Comparator.nullsLast(Comparator.naturalOrder())
                ).reversed())
                .toList();
        int size = goodsList.size();
        OrderGoods orderGoods = goodsList.get(0);
        BigDecimal singleAmount = orderGoods.getTransactionAmount().setScale(2, RoundingMode.HALF_UP);
//        BigDecimal doorsill = appCouponListRespVO.getDoorsill();
//        if(ObjectUtil.isNull(doorsill)){
//            doorsill = BigDecimal.ZERO;
//        }
//        reduceAmount = reduceAmount.add(doorsill);
        if(size < discount){
            appCouponListRespVO.setRemark("订单商品数量不足差" + (discount - size) + "件可用");
            appUserCouponRespVO.getCanNotUseCoupons().add(appCouponListRespVO);
            return;
        }

        if(!Objects.equals(fullReduction,1)){
            if(Objects.equals(doorsillType, UserCouponConstants.DOOR_SILL_TYPE_5)){
                if(reduceAmount.compareTo(singleAmount) >= 0){
                    appCouponListRespVO.setRemark("用券后实付金额高于原价，该券暂不可用");
                    appUserCouponRespVO.getCanNotUseCoupons().add(appCouponListRespVO);
                }else {
                    BigDecimal res = singleAmount.subtract(reduceAmount);
                    appCouponListRespVO.setReduceAmount(res);
                    appCouponListRespVO.setCommodityIds(List.of(singleId));
                    CalculateTheAverageUtil.calculateTheAverage(appCouponListRespVO,1,res);
                    matchingList.add(appCouponListRespVO);
                }
            }else if (Objects.equals(doorsillType, UserCouponConstants.DOOR_SILL_TYPE_6)) {
                BigDecimal ten = new BigDecimal(10);
                BigDecimal subtract = ten.subtract(reduceAmount);
                BigDecimal res = singleAmount.multiply(subtract).divide(ten).setScale(2, RoundingMode.HALF_UP);
                appCouponListRespVO.setReduceAmount(res);
                appCouponListRespVO.setCommodityIds(List.of(singleId));
                CalculateTheAverageUtil.calculateTheAverage(appCouponListRespVO,1,res);
                matchingList.add(appCouponListRespVO);
            }
        }else {
            BigDecimal totalAmount = settlementReqVO.getTransactionAmount();
            if(Objects.equals(doorsillType, UserCouponConstants.DOOR_SILL_TYPE_5)){
                if(reduceAmount.compareTo(singleAmount) >= 0){
                    appCouponListRespVO.setRemark("用券后实付金额高于原价，该券暂不可用");
                    appUserCouponRespVO.getCanNotUseCoupons().add(appCouponListRespVO);
                }else {
                    BigDecimal res = singleAmount.subtract(reduceAmount);
                    BigDecimal subtract = totalAmount.subtract(res);
                    if(subtract.compareTo(appCouponListRespVO.getDoorsill()) >= 0){
                        appCouponListRespVO.setReduceAmount(res);
                        appCouponListRespVO.setCommodityIds(List.of(singleId));
                        CalculateTheAverageUtil.calculateTheAverage(appCouponListRespVO,1,res);
                        matchingList.add(appCouponListRespVO);
                    }else {
                        appCouponListRespVO.setRemark("订单金额差" + appCouponListRespVO.getDoorsill().subtract(subtract) + "元可用");
                        appUserCouponRespVO.getCanNotUseCoupons().add(appCouponListRespVO);
                    }
                }
            }else if (Objects.equals(doorsillType, UserCouponConstants.DOOR_SILL_TYPE_6)) {
                BigDecimal ten = new BigDecimal(10);
                BigDecimal res = singleAmount.multiply(ten.subtract(reduceAmount)).divide(ten).setScale(2, RoundingMode.HALF_UP);
                BigDecimal subtract = totalAmount.subtract(res);
                if(subtract.compareTo(appCouponListRespVO.getDoorsill()) >= 0){
                    appCouponListRespVO.setReduceAmount(res);
                    appCouponListRespVO.setCommodityIds(List.of(singleId));
                    CalculateTheAverageUtil.calculateTheAverage(appCouponListRespVO,1,res);
                    matchingList.add(appCouponListRespVO);
                }else {
                    appCouponListRespVO.setRemark("订单金额差" + appCouponListRespVO.getDoorsill().subtract(subtract) + "元可用");
                    appUserCouponRespVO.getCanNotUseCoupons().add(appCouponListRespVO);
                }
            }
        }
    }
}

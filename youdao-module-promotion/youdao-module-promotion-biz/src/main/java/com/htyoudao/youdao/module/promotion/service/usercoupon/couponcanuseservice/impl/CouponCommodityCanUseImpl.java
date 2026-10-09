package com.htyoudao.youdao.module.promotion.service.usercoupon.couponcanuseservice.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.htyoudao.youdao.framework.mybatis.core.query.MPJLambdaWrapperX;
import com.htyoudao.youdao.module.promotion.context.CommodityIdsContext;
import com.htyoudao.youdao.module.promotion.context.UserCouponCommodityContext;
import com.htyoudao.youdao.module.promotion.controller.app.usercoupon.vo.*;
import com.htyoudao.youdao.module.promotion.service.usercoupon.couponcanuseservice.CouponCanUseService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author dht
 */
@Slf4j
@Service("couponCommodityCanUse")
public class CouponCommodityCanUseImpl implements CouponCanUseService {

    @Resource
    @Qualifier("couponDoorSillCanUse")
    private CouponCanUseService couponCanUseService;

    /**
     * 判断优惠券商品是否可用
     * @param settlementReqVO userCoupon
     * @param userCouponList userCouponList
     * @return List
     */
    @Override
    public AppUserCouponRespVO isDetermineIfItCanBeUsed(SettlementReqVO settlementReqVO, Set<AppCouponCalculateRespVO> userCouponList, AppUserCouponRespVO appUserCouponRespVO) {
//        log.info("优惠券商品可用性校验{}", userCouponList);
        List<OrderGoods> goodsList = settlementReqVO.getGoodsList();
        Set<Long> skuIds = goodsList.stream().map(OrderGoods::getCommodityId).collect(Collectors.toSet());

        //查询优惠券适用的门店
        //1.优惠券ids
        List<Long> couponIds = userCouponList.stream().filter(item -> item.getIsCommonStore() == 2).map(AppCouponCalculateRespVO::getCouponId).toList();
        if(CollectionUtils.isEmpty(couponIds)){
            if(ObjectUtil.isNotEmpty(couponCanUseService) && CollectionUtils.isNotEmpty(userCouponList)){
                return couponCanUseService.isDetermineIfItCanBeUsed(settlementReqVO,userCouponList,appUserCouponRespVO);
            }else {
                return appUserCouponRespVO;
            }
        }



        //可用商品
        Map<Long, List<Long>> canUseMap = CommodityIdsContext.getCommodityId1();
        //不可用商品
        Map<Long, List<Long>> noUseMap = CommodityIdsContext.getCommodityId2();

        Map<Long, List<Long>> commodityId3 = CommodityIdsContext.getCommodityId3();

        Set<AppCouponCalculateRespVO> matchingList = new HashSet<>();
        Set<AppCouponCalculateRespVO> nonMatchingList = new HashSet<>();

        for (AppCouponCalculateRespVO coupon : userCouponList) {
            Integer isCommon = coupon.getIsCommonStore();
            Long couponId = coupon.getCouponId();
            Integer exchangeFlag = coupon.getExchangeFlag();
            Integer couponType = coupon.getCouponType();
            if(ObjectUtil.notEqual(couponType, 5) && ObjectUtil.notEqual(couponType, 2)){
                if (isCommon == 2 && exchangeFlag == 0) {
                    if (ObjectUtil.isNotEmpty(canUseMap) && canUseMap.containsKey(couponId)) {
                        List<Long> commodityIds = canUseMap.get(couponId);
                        Collection<Long> intersection = CollectionUtils.intersection(commodityIds, skuIds);
                        if (CollectionUtils.isNotEmpty(intersection)) {
                            UserCouponCommodityContext.addCanUseCommodities(coupon.getId(), intersection);
                            matchingList.add(coupon);
                        }else {
                            coupon.setRemark("当前商品不可用");
                            nonMatchingList.add(coupon);
                        }
                    }
                } else if (isCommon == 3) {
                    if (ObjectUtil.isNotEmpty(noUseMap) && noUseMap.containsKey(couponId)) {
                        List<Long> commodityIds = noUseMap.get(couponId);
                        Collection<Long> subtract = CollectionUtils.subtract(skuIds, commodityIds);
                        if (CollectionUtils.isNotEmpty(subtract)) {
                            UserCouponCommodityContext.addCanUseCommodities(coupon.getId(), subtract);
                            matchingList.add(coupon);
                        }else {
                            coupon.setRemark("当前商品不可用");
                            nonMatchingList.add(coupon);
                        }
                    }
                } else if(isCommon == 2 && exchangeFlag == 1){
                    if (isCouponMatch(coupon, couponId, skuIds, commodityId3, canUseMap)) {
                        matchingList.add(coupon);
                    } else {
                        coupon.setRemark("当前商品不可用");
                        nonMatchingList.add(coupon);
                    }
                }else {
                    matchingList.add(coupon);
                }
            } else {
                // 不参加活动的商品
                List<Long> ids = goodsList.stream().filter(item -> ObjectUtil.isEmpty(item.getActivityId())).map(OrderGoods::getCommodityId).toList();
                // 参加活动的商品
                List<Long> activityGoodsIds = goodsList.stream().filter(item -> ObjectUtil.isNotEmpty(item.getActivityId())).map(OrderGoods::getCommodityId).toList();

                // 优惠券不可叠加活动
                if(CollectionUtils.isEmpty(ids) && CollectionUtils.isNotEmpty(activityGoodsIds)){
                    coupon.setRemark("此优惠券无法叠加活动");
                    nonMatchingList.add(coupon);
                    continue;
                }

                // 优惠券不可用
                if(!canUseMap.containsKey(coupon.getCouponId())){
                    coupon.setRemark("当前商品不可用");
                    nonMatchingList.add(coupon);
                    continue;
                }
                // 如果下单的商品完全包含优惠券商品 那么
                List<Long> commodityIds = canUseMap.get(coupon.getCouponId());
                boolean b = CollectionUtils.containsAll(activityGoodsIds, commodityIds);
                if(b){
                    coupon.setRemark("此优惠券无法叠加活动");
                    nonMatchingList.add(coupon);
                    continue;
                }
                Collection<Long> intersection = CollectionUtils.intersection(commodityIds, ids);
                Collection<Long> intersection2 = CollectionUtils.intersection(activityGoodsIds, commodityIds);
                // 非活动商品和优惠券商品没有交集  但是下单时候存在活动商品与优惠券商品有交集
                if (CollectionUtils.isEmpty(intersection) && CollectionUtils.isEmpty(intersection2)){
                    coupon.setRemark("当前商品不可用");
                    nonMatchingList.add(coupon);
                    continue;
                }
                // 优惠券商品和活动商品没有交集 优惠券商品和非活动商品也没有交集
                if (CollectionUtils.isEmpty(intersection) && CollectionUtils.isNotEmpty(intersection2)){
                    coupon.setRemark("此优惠券无法叠加活动");
                    nonMatchingList.add(coupon);
                    continue;
                }
                if(exchangeFlag == 1){
                    if (isCouponMatch(coupon, coupon.getCouponId(), skuIds, commodityId3, canUseMap)) {
                        matchingList.add(coupon);
                    } else {
                        coupon.setRemark("当前商品不可用");
                        nonMatchingList.add(coupon);
                    }
                }else {
                    matchingList.add(coupon);
                }
            }
        }


        appUserCouponRespVO.setCanUseCoupons(matchingList);
        appUserCouponRespVO.getCanNotUseCoupons().addAll(nonMatchingList);

        if(ObjectUtil.isNotEmpty(couponCanUseService) && CollectionUtils.isNotEmpty(matchingList)){
            return couponCanUseService.isDetermineIfItCanBeUsed(settlementReqVO,matchingList,appUserCouponRespVO);
        }else {
            return appUserCouponRespVO;
        }
    }



    private boolean isCouponMatch(AppCouponCalculateRespVO coupon, Long couponId, Collection<Long> skuIds,
                                  Map<Long, List<Long>> commodityId3, Map<Long, List<Long>> canUseMap) {
        // 检查commodityId3映射
        if (ObjectUtil.isEmpty(commodityId3) || !commodityId3.containsKey(couponId)) {
            return false;
        }

        // 检查商品交集
        List<Long> commodityIds = commodityId3.get(couponId);
        Collection<Long> intersection = CollectionUtils.intersection(commodityIds, skuIds);
        if (CollectionUtils.isEmpty(intersection)) {
            return false;
        }

        // 检查canUseMap
        if (!canUseMap.containsKey(couponId)) {
            return false;
        }

        // 最终交集检查
        List<Long> usableCommodities = canUseMap.get(couponId);
        Collection<Long> finalIntersection = CollectionUtils.intersection(usableCommodities, skuIds);
        if (CollectionUtils.isEmpty(finalIntersection)) {
            return false;
        }

        // 所有检查通过，添加可用商品
        UserCouponCommodityContext.addCanUseCommodities(coupon.getId(), intersection);
        return true;
    }
}
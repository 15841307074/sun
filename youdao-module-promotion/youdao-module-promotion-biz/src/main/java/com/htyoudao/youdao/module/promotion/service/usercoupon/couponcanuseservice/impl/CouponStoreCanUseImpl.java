package com.htyoudao.youdao.module.promotion.service.usercoupon.couponcanuseservice.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.htyoudao.youdao.module.promotion.controller.app.usercoupon.vo.AppCouponCalculateRespVO;
import com.htyoudao.youdao.module.promotion.controller.app.usercoupon.vo.AppCouponListRespVO;
import com.htyoudao.youdao.module.promotion.controller.app.usercoupon.vo.AppUserCouponRespVO;
import com.htyoudao.youdao.module.promotion.controller.app.usercoupon.vo.SettlementReqVO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.couponstore.CouponStoreDO;
import com.htyoudao.youdao.module.promotion.dal.mysql.couponstore.CouponStoreMapper;
import com.htyoudao.youdao.module.promotion.service.usercoupon.couponcanuseservice.CouponCanUseService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author dht
 */
@Slf4j
@Service("couponStoreCanUse")
public class CouponStoreCanUseImpl implements CouponCanUseService {


    @Resource
    @Qualifier("couponCommodityCanUse")
    private CouponCanUseService couponCanUseService;

    @Resource
    private CouponStoreMapper couponStoreMapper;

    /**
     * 判断优惠券是否符合门店可用
     * @param settlementReqVO settlementReqVO
     * @param userCouponList userCouponList
     * @return UserCouponDO
     */
    @Override
    public AppUserCouponRespVO isDetermineIfItCanBeUsed(SettlementReqVO settlementReqVO, Set<AppCouponCalculateRespVO> userCouponList, AppUserCouponRespVO appUserCouponRespVO) {
        log.info("优惠券是否适用门店{}", settlementReqVO);
        //查询优惠券适用的门店
        //1.优惠券ids
        List<Long> couponIds = userCouponList.stream().filter(item -> item.getIsCommon() == 2).map(AppCouponCalculateRespVO::getCouponId).toList();
        if(CollectionUtils.isEmpty(couponIds)){
            if(ObjectUtil.isNotEmpty(couponCanUseService) && CollectionUtils.isNotEmpty(userCouponList)){
                return couponCanUseService.isDetermineIfItCanBeUsed(settlementReqVO,userCouponList,appUserCouponRespVO);
            }else {
                return appUserCouponRespVO;
            }
        }
        //查询优惠券门店关系表
        QueryWrapper<CouponStoreDO> queryWrapper = new QueryWrapper<>();
        queryWrapper.in("coupon_id", couponIds);
        List<CouponStoreDO> couponStoreList = couponStoreMapper.selectList(queryWrapper);
        Map<Long, List<Long>> map = couponStoreList.stream().collect(
                Collectors.groupingBy(CouponStoreDO::getCouponId, Collectors.mapping(CouponStoreDO::getStoreId, Collectors.toList())));
        for (AppCouponCalculateRespVO coupon : userCouponList) {
            Integer isCommon = coupon.getIsCommon();
            if(isCommon != 1){
                coupon.setStoreIdList(map.get(coupon.getCouponId()));
            }
        }
        //留下门店通用券和门店可用券
//        List<AppCouponListRespVO> list = userCouponList.stream().filter(item ->
//                item.getIsCommon() == 1 ||
//                        (item.getIsCommon() == 2 && (CollectionUtils.isNotEmpty(item.getStoreIdList()) && item.getStoreIdList().contains(settlementReqVO.getStoreId())))).toList();
        Map<Boolean, Set<AppCouponCalculateRespVO>> partitioned = userCouponList.stream()
                .collect(Collectors.partitioningBy(
                        item -> item.getIsCommon() == 1 ||
                                (item.getIsCommon() == 2 &&
                                        CollectionUtils.isNotEmpty(item.getStoreIdList()) &&
                                        item.getStoreIdList().contains(settlementReqVO.getStoreId())),
                        Collectors.toSet()
                ));

        Set<AppCouponCalculateRespVO> matchingList = partitioned.get(true);
        Set<AppCouponCalculateRespVO> nonMatchingList = partitioned.get(false);
        for (AppCouponCalculateRespVO appCouponListRespVO : nonMatchingList) {
            appCouponListRespVO.setRemark("当前门店不可用");
        }

        appUserCouponRespVO.setCanUseCoupons(matchingList);
        appUserCouponRespVO.getCanNotUseCoupons().addAll(nonMatchingList);
        //区分门店通用券和门店可用券

        if(ObjectUtil.isNotEmpty(couponCanUseService) && CollectionUtils.isNotEmpty(matchingList)){
            return couponCanUseService.isDetermineIfItCanBeUsed(settlementReqVO,matchingList,appUserCouponRespVO);
        }else {
            return appUserCouponRespVO;
        }
    }
}
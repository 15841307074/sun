package com.htyoudao.youdao.module.promotion.service.couponcommodity;

import com.htyoudao.youdao.module.promotion.controller.app.usercoupon.vo.AppCouponCommodityVO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.couponcommodity.CouponCommodityDO;

import java.util.List;

/**
 * 优惠券门店关系 Service 接口
 *
 * @author 13149747939
 */
public interface CouponCommodityService {


    /**
     * 批量插入优惠券门店关系
     * @param couponCommodities couponCommodities
     */
    void insertBatch(List<CouponCommodityDO> couponCommodities);

    /**
     * 根据优惠券id删除优惠券门店关系
     * @param couponId couponId
     */
    void deleteByCouponId(Long couponId);

    /**
     * 根据优惠券id查询优惠券门店关系
     * @param id id
     * @return CouponCommodityDO
     */
    List<CouponCommodityDO> selectByCouponId(Long id);

    List<CouponCommodityDO> selectByCommodityId(Long commodityId);

    List<CouponCommodityDO> selectByCouponIds(List<Long> respGoodCouponIds);

    List<AppCouponCommodityVO> selectCouponCommodity(Long couponId);

    Boolean selectCouponHaveCommodity(Long commodityId);

    Boolean updateCommodityName(Long commodityId, String commodityName);
}
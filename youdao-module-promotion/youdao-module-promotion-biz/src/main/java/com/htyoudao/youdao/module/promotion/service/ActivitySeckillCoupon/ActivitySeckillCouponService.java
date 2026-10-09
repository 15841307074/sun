package com.htyoudao.youdao.module.promotion.service.ActivitySeckillCoupon;

import com.baomidou.mybatisplus.extension.service.IService;
import com.htyoudao.youdao.module.promotion.controller.admin.activitySeckill.vo.ActivitySeckillCouponRespVO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.activitySeckill.ActivitySeckillCouponDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.goodcoupon.GoodCouponDO;

import java.util.List;

public interface ActivitySeckillCouponService extends IService<ActivitySeckillCouponDO> {
    void createBatch(List<ActivitySeckillCouponDO> activitySeckillCommodityDOS);

    void updateBatch(List<ActivitySeckillCouponDO> activitySeckillCommodityDOS);

    List<ActivitySeckillCouponDO> selectByActivityId(Long activityId);

    void deleteByActivityId(Long id);

    Integer getLimitByCache(Long activityId, Long couponId);

    GoodCouponDO getCouponByCache(Long activityId, Long couponId);

    List<ActivitySeckillCouponRespVO> seckillCouponList(Long storeId, Long activityId, Integer times);


    /**
     * 优惠券信息变更时，更新活动缓存
     * @param couponId
     */
    void reloadActivityCache(Long couponId);

}

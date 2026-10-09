package com.htyoudao.youdao.module.promotion.service.activityJDCoupon;

import com.baomidou.mybatisplus.extension.service.IService;
import com.htyoudao.youdao.module.promotion.controller.admin.activitySeckill.vo.ActivitySeckillCouponRespVO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.activityJD.ActivityJDCouponDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.activitySeckill.ActivitySeckillCouponDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.goodcoupon.GoodCouponDO;

import java.util.List;

public interface ActivityJDCouponService {
    void createBatch(List<ActivityJDCouponDO> activityJDCommodityDOS);

    void updateBatch(List<ActivityJDCouponDO> activityJDCommodityDOS);

    List<ActivityJDCouponDO> selectByActivityId(Long activityId);

    void deleteByActivityId(Long id);

    List<ActivityJDCouponDO> selectActivityByCouponId(Long id);
}

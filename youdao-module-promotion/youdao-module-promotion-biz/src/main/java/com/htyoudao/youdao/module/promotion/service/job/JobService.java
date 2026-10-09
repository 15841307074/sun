package com.htyoudao.youdao.module.promotion.service.job;

public interface JobService {


    void screenAdvertisements();

    void memberDayCoupon();

    void cancelDouyinCoupon();

    void lotteryReturnReal();

    void scheduledSendMessage();

    void nocCouponDataToRedis();

    void xxjobSpecifyDateCoupon();

    void automaticDistributionOnMemberDaysJobHandler();

    void lotteryPoolReset();

    void resetPoint();

    void automaticPointsGoodsJobHandler();

    void memberCardBenefitJob();
}

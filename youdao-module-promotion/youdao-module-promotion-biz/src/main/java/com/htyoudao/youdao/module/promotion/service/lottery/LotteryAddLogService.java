package com.htyoudao.youdao.module.promotion.service.lottery;


import com.htyoudao.youdao.module.promotion.dal.dataobject.goodcoupon.GoodCouponDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.lottery.LotteryLogDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.points.PointsLogDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.usercoupon.UserCouponDO;

import java.time.LocalTime;

public interface LotteryAddLogService {
    /**
     * 添加抽奖记录
     */

    public void addLotteryLog(LotteryLogDO log );
    /**
     * 添加积分记录
     */
    public void addPointsLog(PointsLogDO log );
    /**
     * 查询抽奖次数
     */
    public int selectLotteryNum(Long memberId,Long lotteryId );
    /**
     * 查询优惠卷
     */
    public GoodCouponDO getGoodCoupon(Long awardId);
    /**
     * 添加优惠卷
     */
    public void addCoupon(UserCouponDO userCouponDO);
    /**
     * 查询总抽奖次数
     */
    public int selectLotterySum(Long memberId,Long lotteryId );
    /**
     * 查询抽奖次数按场次
     */
    public int selectLotteryNumBySession(Long memberId, Long lotteryId, LocalTime startTime, LocalTime endTime);
}

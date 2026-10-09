package com.htyoudao.youdao.module.promotion.api.lottery;

import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.module.promotion.api.goodcoupon.DTO.GoodCouponDataDTO;
import com.htyoudao.youdao.module.promotion.api.goodcoupon.DTO.GoodCouponStoreDTO;
import com.htyoudao.youdao.module.promotion.api.goodcoupon.GoodCouponApi;
import com.htyoudao.youdao.module.promotion.api.goodcoupon.VO.GoodCouponCardVO;
import com.htyoudao.youdao.module.promotion.api.lottery.DTO.LotteryDTO;
import com.htyoudao.youdao.module.promotion.service.couponstore.CouponStoreService;
import com.htyoudao.youdao.module.promotion.service.goodcoupon.GoodCouponService;
import com.htyoudao.youdao.module.promotion.service.lottery.LotteryMobileService;
import com.htyoudao.youdao.module.promotion.service.lottery.LotterySettingService;
import jakarta.annotation.Resource;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.Map;


@DubboService
@Validated
public class LotteryApiImpl implements LotteryApi {
    @Resource
    private LotteryMobileService lotteryMobileService;


    @Override
    public Map<Long,List<Long>> getLotteryList( Long storeId ,Long memberId) {
        return lotteryMobileService.getLotteryList(storeId ,  memberId);
    }
}

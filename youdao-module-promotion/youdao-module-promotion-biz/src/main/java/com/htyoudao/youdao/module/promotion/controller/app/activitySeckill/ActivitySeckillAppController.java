package com.htyoudao.youdao.module.promotion.controller.app.activitySeckill;

import static com.htyoudao.youdao.framework.common.pojo.CommonResult.success;

import com.htyoudao.youdao.framework.common.exception.ServiceException;
import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.module.promotion.api.enums.ErrorCodeConstants;
import com.htyoudao.youdao.module.promotion.controller.admin.activitySeckill.vo.ActivitySeckillCouponRespVO;
import com.htyoudao.youdao.module.promotion.controller.admin.activitySeckill.vo.ActivitySeckillRespVO;
import com.htyoudao.youdao.module.promotion.controller.admin.activitySeckill.vo.ActivitySeckillTimeRespVO;
import com.htyoudao.youdao.module.promotion.controller.app.activitySeckill.vo.ActivitySeckillAppRespVO;
import com.htyoudao.youdao.module.promotion.controller.app.activitySeckill.vo.ActivitySeckillAppShareVO;
import com.htyoudao.youdao.module.promotion.controller.app.activitySeckill.vo.ActivitySeckillAppTimeResqVO;
import com.htyoudao.youdao.module.promotion.service.ActivitySeckillCoupon.ActivitySeckillCouponService;
import com.htyoudao.youdao.module.promotion.service.activity.ActivityService;
import com.htyoudao.youdao.module.promotion.service.activitySeckill.ActivitySeckillService;
import com.htyoudao.youdao.module.promotion.service.activitySeckill.SeckillActivityCacheService;
import com.htyoudao.youdao.module.promotion.util.TimeValidationUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.annotation.security.PermitAll;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@Tag(name = "小程序 - 秒杀活动")
@RestController
@RequestMapping("/promotion/app/activity/seckill")
@RequiredArgsConstructor
public class ActivitySeckillAppController {

    @Resource
    private SeckillActivityCacheService cacheService;

    @Resource
    private ActivityService activityService;

    @Resource
    private ActivitySeckillService activitySeckillService;

    @Resource
    private ActivitySeckillCouponService seckillCouponService;

    @Resource
    private RedisTemplate<String, String> redisTemplate;

    @GetMapping("/info")
    @Operation(summary = "小程序秒杀活动详情")
    @PermitAll
    public CommonResult<ActivitySeckillAppRespVO> appActivitySeckillInfo(
        @RequestParam("id") Long id,
        @RequestParam("storeId") Long storeId) {

        ActivitySeckillRespVO activity = cacheService.getActivity(id);

        if (activity == null) {
            throw new ServiceException(ErrorCodeConstants.SECKILL_CACHE_NOT_FOUND);
        }
//        if (!Objects.equals(activity.getActivityStore(),1) && !activity.getStoreIds().contains(storeId)) {
//            throw new ServiceException(ErrorCodeConstants.SECKILL_STORE_NOT_ALLOW);
//        }
        ActivitySeckillAppRespVO activitySeckillRespVO = convertCacheToAppVO(activity);
        return success(activitySeckillRespVO);
    }

    private ActivitySeckillAppRespVO convertCacheToAppVO(ActivitySeckillRespVO activity) {

        ActivitySeckillAppRespVO appRespVO = new ActivitySeckillAppRespVO();
        BeanUtils.copyProperties(activity, appRespVO);

        //设置日期是否满足条件
        appRespVO.setTimeValid(
            TimeValidationUtil.isTimeValid(
                activity.getStartDate(), activity.getEndDate(), activity.getDayNumberList(),
                activity.getWeekNumberList(), null)
        );

        List<ActivitySeckillTimeRespVO> times = activity.getActivitySeckillTimeRespVOS();

        List<ActivitySeckillAppTimeResqVO> timeResqVOS = new ArrayList<>();
        LocalTime now = LocalTime.now();

        for (ActivitySeckillTimeRespVO time : times) {
            ActivitySeckillAppTimeResqVO appTimeResqVO = new ActivitySeckillAppTimeResqVO();
            BeanUtils.copyProperties(time, appTimeResqVO);

            int startHour = appTimeResqVO.getStartTime();
            int endHour = appTimeResqVO.getEndTime();

            LocalTime startTime = LocalTime.of(startHour, 0);
            LocalTime endTime;

            // 如果结束时间是24时，转换为23:59:59
            if (endHour == 24) {
                endTime = LocalTime.of(23, 59, 59);
            } else {
                endTime = LocalTime.of(endHour, 0);
            }

            // 计算时间差（秒）
            long secondsToStart = now.until(startTime, ChronoUnit.SECONDS);
            long secondsToEnd = now.until(endTime, ChronoUnit.SECONDS);

            if (now.isBefore(startTime) && now.isBefore(endTime)) {
                appTimeResqVO.setStatus(1);
                appTimeResqVO.setSecondsCount(secondsToStart);
            } else if (now.isBefore(endTime)) {
                appTimeResqVO.setStatus(2);
                appTimeResqVO.setSecondsCount(secondsToEnd);
            } else {
                appTimeResqVO.setStatus(3);
                appTimeResqVO.setSecondsCount(0L);
            }
            timeResqVOS.add(appTimeResqVO);
        }
        appRespVO.setTimes(timeResqVOS);
        return appRespVO;
    }

    @GetMapping("/getShareVO")
    @Operation(summary = "小程序秒杀活动分享详情")
    @PermitAll
    public CommonResult<ActivitySeckillAppShareVO> getShareVO(@RequestParam("activityId") Long activityId){
        ActivitySeckillAppShareVO appShareVO = activitySeckillService.getShareVO(activityId);
        return success(appShareVO);
    }



    @GetMapping("/seckill/coupon/list")
    @Operation(summary = "小程序秒杀优惠券列表")
    @PermitAll
    public CommonResult<List<ActivitySeckillCouponRespVO>> seckillCouponList(Long storeId, Long activityId, Integer times) {
        List<ActivitySeckillCouponRespVO> list = seckillCouponService.seckillCouponList(storeId, activityId, times);
        return success(list);
    }


    @Operation(summary = "秒杀弹幕")
    @GetMapping("/seckill/buy/list")
    @PermitAll
    public CommonResult<List<String>> couponSeckillBuyList(Long activityId, Long storeId) {
        try {
            String key = "claim_coupon_queue:" + activityId;
            // 获取最新的10条弹幕
            List<String> danmakuList = redisTemplate.opsForList().range(key, 0, 9);
            return CommonResult.success(danmakuList);
        } catch (Exception e) {
            return CommonResult.success(List.of());
        }
    }

}

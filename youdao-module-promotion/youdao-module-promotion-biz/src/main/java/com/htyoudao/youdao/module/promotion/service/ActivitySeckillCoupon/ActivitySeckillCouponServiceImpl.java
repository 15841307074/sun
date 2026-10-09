package com.htyoudao.youdao.module.promotion.service.ActivitySeckillCoupon;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.htyoudao.youdao.framework.common.exception.ServiceException;
import com.htyoudao.youdao.module.promotion.api.enums.ErrorCodeConstants;
import com.htyoudao.youdao.module.promotion.controller.admin.activitySeckill.vo.ActivitySeckillCouponRespVO;
import com.htyoudao.youdao.module.promotion.controller.admin.activitySeckill.vo.ActivitySeckillRespVO;
import com.htyoudao.youdao.module.promotion.controller.admin.activitySeckill.vo.ActivitySeckillTimeRespVO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.activity.ActivityDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.activitySeckill.ActivitySeckillCouponDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.activitySeckillCommodity.ActivitySeckillCommodityDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.goodcoupon.GoodCouponDO;
import com.htyoudao.youdao.module.promotion.dal.mysql.activity.ActivityMapper;
import com.htyoudao.youdao.module.promotion.dal.mysql.activitySeckill.ActivitySeckillCouponMapper;
import com.htyoudao.youdao.module.promotion.dal.mysql.goodcoupon.GoodCouponMapper;
import com.htyoudao.youdao.module.promotion.service.activitySeckill.ActivitySeckillService;
import com.htyoudao.youdao.module.promotion.service.activitySeckill.SeckillActivityCacheService;
import com.htyoudao.youdao.module.promotion.service.activitySeckill.SeckillCouponStockCacheService;
import com.htyoudao.youdao.module.promotion.service.activitySeckill.StockPreheatService;
import jakarta.annotation.Resource;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

@Slf4j
@Service
@RequiredArgsConstructor
public class ActivitySeckillCouponServiceImpl extends ServiceImpl<ActivitySeckillCouponMapper, ActivitySeckillCouponDO> implements ActivitySeckillCouponService {

    @Resource
    private ActivitySeckillCouponMapper couponMapper;

    @Resource
    private SeckillCouponStockCacheService stockCacheService;

    @Resource
    private StockPreheatService stockPreheatService;

    @Resource
    private SeckillActivityCacheService cacheService;

    @Resource
    private GoodCouponMapper goodCouponMapper;

    @Resource
    private ActivitySeckillService seckillService;

    @Resource
    private ActivityMapper activityMapper;



    @Override
    public void reloadActivityCache(Long couponId) {
        List<ActivitySeckillCouponDO> seckillCouponDOS = couponMapper.selectList(ActivitySeckillCouponDO::getCouponId,
            couponId);

        if (CollectionUtils.isEmpty(seckillCouponDOS)){
            return;
        }

        for (ActivitySeckillCouponDO seckillCouponDO : seckillCouponDOS) {
            try {
                ActivitySeckillRespVO respVO = seckillService.selectInfo(seckillCouponDO.getActivityId());
                cacheService.cacheActivity(respVO);
            } catch (Exception e) {
                log.warn("activity:{} cache error", seckillCouponDO.getActivityId(), e);
            }
        }
    }

    @Override
    public void createBatch(List<ActivitySeckillCouponDO> activitySeckillCommodityDOS) {
        couponMapper.insertBatch(activitySeckillCommodityDOS);
    }

    @Override
    public void updateBatch(List<ActivitySeckillCouponDO> couponDOS) {
        if (CollectionUtils.isEmpty(couponDOS)){
            return;
        }
        Long activityId = couponDOS.get(0).getActivityId();

        //若活动库存修改后 去掉对应的商品库存缓存， 重新设置
        resetCache(activityId, couponDOS);

        List<ActivitySeckillCouponDO> insertList = new ArrayList<>();
        List<ActivitySeckillCouponDO> updateList = new ArrayList<>();
        List<Long> currentIds = new ArrayList<>();
        for (ActivitySeckillCouponDO couponDO : couponDOS) {
            if (couponDO.getId() == null) {
                insertList.add(couponDO);
            }else {
                currentIds.add(couponDO.getId());
                LambdaQueryWrapper<ActivitySeckillCouponDO> queryWrapper = new LambdaQueryWrapper<>();
                queryWrapper.eq(ActivitySeckillCouponDO::getId, couponDO.getId());
                if (couponMapper.exists(queryWrapper)) {
                    updateList.add(couponDO);
                }else {
                    couponDO.setId(null);
                    insertList.add(couponDO);
                }
            }
        }



        LambdaQueryWrapper<ActivitySeckillCouponDO> deleteWrapper = new LambdaQueryWrapper<>();
        if (!currentIds.isEmpty()) {
            deleteWrapper.notIn(ActivitySeckillCouponDO::getId, currentIds);
        }

        deleteWrapper.eq(ActivitySeckillCouponDO::getActivityId,activityId);
        couponMapper.delete(deleteWrapper);
        if (!insertList.isEmpty()) {
            couponMapper.insertBatch(insertList);
        }
        if (!updateList.isEmpty()) {
            couponMapper.updateBatch(updateList);
        }
    }

    @Override
    public List<ActivitySeckillCouponDO> selectByActivityId(Long activityId) {
        LambdaQueryWrapper<ActivitySeckillCouponDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ActivitySeckillCouponDO::getActivityId, activityId);
        return couponMapper.selectList(queryWrapper);
    }

    @Override
    public void deleteByActivityId(Long id) {
        LambdaQueryWrapper<ActivitySeckillCouponDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ActivitySeckillCouponDO::getActivityId, id);
        couponMapper.delete(queryWrapper);
    }

    @Override
    public Integer getLimitByCache(Long activityId, Long couponId) {

        ActivitySeckillRespVO activity = cacheService.getActivity(activityId);

        if (activity == null) {
            log.error("");
            throw new ServiceException(ErrorCodeConstants.SECKILL_NOT_FOUND);
        }

        ActivitySeckillCouponRespVO couponRespVO = activity.getActivitySeckillCouponRespVOS().stream()
            .filter(c -> Objects.equals(c.getCouponId(), couponId))
            .findFirst().orElseThrow(() -> new ServiceException(ErrorCodeConstants.SECKILL_NOT_FOUND));


        return couponRespVO.getLimitPerItem();
    }

    @Override
    public GoodCouponDO getCouponByCache(Long activityId, Long couponId) {
        ActivitySeckillRespVO activity = cacheService.getActivity(activityId);

        if (activity == null) {
            log.error("");
            throw new ServiceException(ErrorCodeConstants.SECKILL_NOT_FOUND);
        }
        ActivitySeckillCouponRespVO couponRespVO = activity.getActivitySeckillCouponRespVOS().stream()
                .filter(c -> Objects.equals(c.getCouponId(), couponId))
                .findFirst().orElseThrow(() -> new ServiceException(ErrorCodeConstants.SECKILL_NOT_FOUND));
        return couponRespVO.getCoupon();
    }

    @Override
    public List<ActivitySeckillCouponRespVO> seckillCouponList(Long storeId, Long activityId, Integer times) {

        ActivitySeckillRespVO activity = cacheService.getActivity(activityId);

        ActivitySeckillTimeRespVO timeRespVO = activity.getActivitySeckillTimeRespVOS().stream()
            .filter(a -> a.getTimes().equals(times))
            .findFirst().orElse(null);

        List<ActivitySeckillCouponRespVO> list = activity.getActivitySeckillCouponRespVOS();

        if (timeRespVO == null) {
            log.warn("activity:{} time:{} not found", activityId, times);
            return list;
        }

        //移除下架优惠券
        list.removeIf(c -> Objects.equals(c.getCoupon().getIsGround(), 0));

        int hour = LocalTime.now().getHour();
        // 进行中场次，重新设置库存
        if (hour >= timeRespVO.getStartTime() && hour <= timeRespVO.getEndTime()) {
            for (ActivitySeckillCouponRespVO couponRespVO : list) {
                Integer currentStock = stockCacheService.getCurrentStock(storeId, activityId,
                    couponRespVO.getCouponId(), times);

                if (currentStock == null){
                    ActivityDO activityDO = activityMapper.selectById(activityId);
                    if (activityDO == null){
                        log.error("活动信息不存在:{}", activityId);
                        break;
                    }
                    stockPreheatService.preheatStock(times, List.of(activityDO));
                    currentStock = stockCacheService.getCurrentStock(storeId, activityId,
                        couponRespVO.getCouponId(), times);
                }
                couponRespVO.setActivityStock(currentStock);
            }
        }

        return list;
    }


    private void resetCache(Long activityId, List<ActivitySeckillCouponDO> seckillCouponDOS) {

        List<ActivitySeckillCouponDO> oldCoupons = couponMapper.selectList(
            ActivitySeckillCouponDO::getActivityId, activityId);

        if (CollectionUtils.isEmpty(oldCoupons)){
            return;
        }

        Map<Long, Integer> oldAcStockMap = oldCoupons.stream()
            .collect(Collectors.toMap(ActivitySeckillCouponDO::getCouponId,
                ActivitySeckillCouponDO::getActivityStock));

        for (ActivitySeckillCouponDO doActivityStock : seckillCouponDOS) {
            Long commodityId = doActivityStock.getCouponId();
            //，若活动库存修改后 去掉对应的商品库存缓存， 重新设置
            if (!Objects.equals(oldAcStockMap.get(commodityId), doActivityStock.getActivityStock())){
                stockCacheService.deleteStockCache(activityId, commodityId);
            }
        }
    }

}

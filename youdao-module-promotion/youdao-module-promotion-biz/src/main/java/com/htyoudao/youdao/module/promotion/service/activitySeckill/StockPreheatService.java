package com.htyoudao.youdao.module.promotion.service.activitySeckill;

import com.alibaba.fastjson.JSON;
import com.htyoudao.youdao.framework.context.BusinessContextHolder;
import com.htyoudao.youdao.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.htyoudao.youdao.module.promotion.api.enums.activity.ActivityTypeEnum;
import com.htyoudao.youdao.module.promotion.dal.dataobject.activity.ActivityDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.activitySeckill.ActivitySeckillCouponDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.activitySeckill.ActivitySeckillDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.activitySeckillCommodity.ActivitySeckillCommodityDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.activitySeckillTime.ActivitySeckillTimeDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.activityStore.ActivityStoreDO;
import com.htyoudao.youdao.module.promotion.dal.mysql.activity.ActivityMapper;
import com.htyoudao.youdao.module.promotion.dal.mysql.activitySeckill.ActivitySeckillCouponMapper;
import com.htyoudao.youdao.module.promotion.dal.mysql.activitySeckill.ActivitySeckillMapper;
import com.htyoudao.youdao.module.promotion.dal.mysql.activitySeckillCommodity.ActivitySeckillCommodityMapper;
import com.htyoudao.youdao.module.promotion.dal.mysql.activitySeckillTime.ActivitySeckillTimeMapper;
import com.htyoudao.youdao.module.promotion.dal.mysql.activityStore.ActivityStoreMapper;
import com.htyoudao.youdao.module.promotion.util.TimeValidationUtil;
import com.htyoudao.youdao.module.system.api.store.StoreApi;
import com.htyoudao.youdao.module.system.api.store.dto.StoreInfoDTO;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

@Slf4j
@Service
public class StockPreheatService {

    @Autowired
    private SeckillStockCacheService commodityCacheService;

    @Autowired
    private SeckillCouponStockCacheService couponCacheService;

    @Autowired
    private ActivityMapper activityMapper;

    @Autowired
    private ActivitySeckillMapper activitySeckillMapper;

    @Autowired
    private ActivitySeckillTimeMapper activitySeckillTimeMapper;

    @Autowired
    private ActivitySeckillCommodityMapper seckillCommodityMapper;

    @Autowired
    private ActivitySeckillCouponMapper seckillCouponMapper;

    @Autowired
    private ActivityStoreMapper activityStoreMapper;

    @DubboReference
    private StoreApi storeApi;

    public void preheatStock(Integer time){

        LambdaQueryWrapperX<ActivityDO> queryWrapper = new LambdaQueryWrapperX();
        queryWrapper.eq(ActivityDO::getActivityType, ActivityTypeEnum.SEC_KILL.getCode());
        queryWrapper.eq(ActivityDO::getIsEnabled, 1);
        List<ActivityDO> activityDOS = activityMapper.selectList(queryWrapper);
        if (CollectionUtils.isEmpty(activityDOS)) {
            log.info("不需要预热缓存");
            return;
        }

        preheatStock(time, activityDOS);
    }

    /**
     * 根据场次 预热活动
     * @param time
     */
    public void preheatStock(Integer time, List<ActivityDO> activityDOS) {

        List<ActivitySeckillDO> list = getNeedPreheatActivity(time, activityDOS);

        log.info("=====项目:{},{}点场 预热缓存开始!,count:{}",BusinessContextHolder.getBusinessId(), time, list.size());

        if (CollectionUtils.isEmpty(list)){
            return;
        }
        Map<Long, List<Long>> needStoreIdMap = getNeedStoreMap(activityDOS);

        log.info("====={}点场 预热信息{}", time, JSON.toJSONString(needStoreIdMap));


        // 3. 预热库存
        for (ActivitySeckillDO item : list) {

            List<ActivitySeckillCommodityDO> commodityDOS = seckillCommodityMapper.selectList(
                ActivitySeckillCommodityDO::getActivityId, item.getActivityId());

            List<ActivitySeckillCouponDO> seckillCouponDOS = seckillCouponMapper.selectList(
                ActivitySeckillCouponDO::getActivityId, item.getActivityId());


            if (!CollectionUtils.isEmpty(commodityDOS)){
                for (ActivitySeckillCommodityDO commodityDO : commodityDOS) {
                    List<Long> storeIds = needStoreIdMap.get(commodityDO.getActivityId());
                    log.info("预热活动:{},商品:{},门店:{},库存:",item.getActivityId(), commodityDO.getCommodityId(), commodityDO.getActivityStock());

                    commodityCacheService.initSessionStockBatch(
                        storeIds,
                        item.getActivityId(),
                        commodityDO.getCommodityId(),
                        time,
                        commodityDO.getActivityStock()
                    );
                }
            }


            if (!CollectionUtils.isEmpty(seckillCouponDOS)){
                for (ActivitySeckillCouponDO couponDO : seckillCouponDOS) {
                    log.info("预热活动:{},优惠券:{},库存:{}",item.getActivityId(),couponDO.getCouponId(), couponDO.getActivityStock());
                    List<Long> storeIds = needStoreIdMap.get(couponDO.getActivityId());

                    couponCacheService.initSessionStockBatch(
                        storeIds,
                        item.getActivityId(),
                        couponDO.getCouponId(),
                        time,
                        couponDO.getActivityStock()
                    );
                }
            }




        }
    }

    /**
     * 根据场次号，获取需要加载到缓存的秒杀活动
     * 1.上下架有效性判断
     * 2.时间条件有效性
     * 3.场次判断
     * @param time
     * @param activityDOS
     * @return
     */
    public List<ActivitySeckillDO> getNeedPreheatActivity(Integer time, List<ActivityDO> activityDOS) {
        activityDOS = activityDOS.stream()
            .filter(a -> Objects.equals(a.getIsEnabled(), 1))
            .filter(a ->
            TimeValidationUtil.isTimeValid(
                a.getStartDate(), a.getEndDate(),
                a.getDayNumbers(), a.getWeekNumbers(), a.getTimeRange()
            ))
            .toList();

        if (CollectionUtils.isEmpty(activityDOS)) {
            log.info("过滤时间后 不需要预热缓存");
            return List.of();
        }
        List<Long> activityIds = activityDOS.stream().map(ActivityDO::getId).toList();
        List<ActivitySeckillDO> list = activitySeckillMapper.selectList(ActivitySeckillDO::getActivityId, activityIds);
        Iterator<ActivitySeckillDO> iterator = list.iterator();
        while (iterator.hasNext()) {
            ActivitySeckillDO next = iterator.next();
            List<ActivitySeckillTimeDO> timeDOS = activitySeckillTimeMapper.selectList(
                ActivitySeckillTimeDO::getActivityId, next.getActivityId());
            Set<Integer> collect = timeDOS.stream().map(ActivitySeckillTimeDO::getTimes).collect(Collectors.toSet());
            if (!collect.contains(time)) {
                iterator.remove();
            }
        }

        return list;
    }

    private Map<Long, List<Long>> getNeedStoreMap(List<ActivityDO> activityDOS) {
        Map<Long, List<Long>> map = new HashMap<>();
        List<ActivityDO> list = activityDOS.stream().filter(a -> a.getActivityStore() == 1).toList();
        if (!CollectionUtils.isEmpty(list)) {
            List<Long> storeIds = storeApi.getStoreListByBusinessId(BusinessContextHolder.getBusinessId())
                .getCheckedData()
                .stream().map(StoreInfoDTO::getStoreId)
                .toList();
            for (ActivityDO activityDO : list) {
                map.put(activityDO.getId(), storeIds);
            }
        }

        List<ActivityDO> otherList = activityDOS.stream().filter(a -> a.getActivityStore() == 0).toList();
        for (ActivityDO activityDO : otherList) {
            List<ActivityStoreDO> activityStoreDOS = activityStoreMapper.selectList(ActivityStoreDO::getActivityId,
                activityDO.getId());
            map.put(activityDO.getId(), activityStoreDOS.stream().map(ActivityStoreDO::getStoreId).toList());
        }

        return map;
    }
}
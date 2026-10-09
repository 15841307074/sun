package com.htyoudao.youdao.module.promotion.service.activitySeckill;

import com.alibaba.fastjson.JSON;
import com.htyoudao.youdao.module.promotion.api.activity.DTO.ActivitySeckillRespDTO;
import com.htyoudao.youdao.module.promotion.controller.admin.activitySeckill.vo.ActivitySeckillRespVO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.activity.ActivityDO;
import com.htyoudao.youdao.module.promotion.dal.mysql.activity.ActivityMapper;
import com.htyoudao.youdao.module.promotion.util.TimeValidationUtil;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class SeckillApiService {

    @Autowired
    private SeckillStockCacheService stockCacheService;

    @Autowired
    private ActivitySeckillService activitySeckillService;

    @Autowired
    private SeckillActivityCacheService activityCacheService;

    @Autowired
    private ActivityMapper activityMapper;

    @Autowired
    private StockPreheatService stockPreheatService;

    public ActivitySeckillRespDTO getActivity(Long activityId) {
        ActivitySeckillRespVO activity = activityCacheService.getActivity(activityId);
        if (activity == null) {
            log.warn("activity not found:{}", activityId);
            return null;
        }
        String jsonString = JSON.toJSONString(activity);
        ActivitySeckillRespDTO respDTO = JSON.parseObject(jsonString, ActivitySeckillRespDTO.class);

        //设置日期是否满足条件
        respDTO.setTimeValid(
            TimeValidationUtil.isTimeValid(
                activity.getStartDate(), activity.getEndDate(), activity.getDayNumberList(),
                activity.getWeekNumberList(), null)
        );

        return respDTO;
    }

    public Map<Long, Integer> seckillStock(Long storeId, Long activityId, Integer time,
        Collection<Long> commodityIds) {

        Map<Long, Integer> resultMap = new HashMap<>();
        for (Long commodityId : commodityIds) {
            Integer currentStock = stockCacheService.getCurrentStock(storeId, activityId, commodityId, time);
            if (currentStock == null){
                log.warn("未获取到缓存,根据场次重新设置");
                resetCache(activityId, time);
                currentStock = stockCacheService.getCurrentStock(storeId, activityId, commodityId, time);
            }
            resultMap.put(commodityId, currentStock);
        }

        return resultMap;
    }

    private void resetCache(Long activityId, Integer time) {
        ActivityDO activityDO = activityMapper.selectById(activityId);
        if (activityDO == null){
            log.error("活动信息不存在:{}", activityId);
            return;
        }
        stockPreheatService.preheatStock(time, List.of(activityDO));
    }

}
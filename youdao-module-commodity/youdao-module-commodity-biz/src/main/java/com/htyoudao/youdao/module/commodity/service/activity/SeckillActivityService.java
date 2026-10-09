package com.htyoudao.youdao.module.commodity.service.activity;

import static com.htyoudao.youdao.framework.common.exception.util.ServiceExceptionUtil.exception;

import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.module.commodity.controller.app.product.vo.ActivitySeckillCommodityRespVO;
import com.htyoudao.youdao.module.commodity.controller.app.product.vo.ActivitySeckillRespVO;
import com.htyoudao.youdao.module.commodity.controller.app.product.vo.ActivitySeckillTimeRespVO;
import com.htyoudao.youdao.module.commodity.controller.app.product.vo.SeckillSpuVO;
import com.htyoudao.youdao.module.commodity.dal.dto.SpuDto;
import com.htyoudao.youdao.module.commodity.dal.dto.StoreCategoryDTO;
import com.htyoudao.youdao.module.commodity.enums.ClientType;
import com.htyoudao.youdao.module.commodity.service.storeSpu.ICommodityStoreSpuService;
import com.htyoudao.youdao.module.promotion.api.activity.ActivityApi;
import com.htyoudao.youdao.module.promotion.api.activity.DTO.ActivitySeckillCommodityRespDTO;
import com.htyoudao.youdao.module.promotion.api.activity.DTO.ActivitySeckillRespDTO;
import com.htyoudao.youdao.module.promotion.api.activity.DTO.ActivitySeckillStockDTO;
import com.htyoudao.youdao.module.promotion.api.activity.DTO.ActivitySeckillTimeRespDTO;
import jakarta.annotation.Resource;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.annotation.Validated;

/**
 * 商品活动 Service 实现类
 *
 * @author hhhh
 */
@DubboService
@Service
@Validated
@Slf4j
public class SeckillActivityService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Resource
    private ICommodityStoreSpuService commodityStoreSpuService;

    @DubboReference
    private ActivityApi activityApi;


    public List<SeckillSpuVO> seckillList(Long storeId, Long activityId, Integer times) {

        //活动缓存
        ActivitySeckillRespDTO activity = activityApi.selectSeckillActivity(activityId)
                .getCheckedData();
        if (activity == null) {
            return List.of();
        }
        Map<Long, ActivitySeckillCommodityRespDTO> collect = activity.getActivitySeckillCommodityRespVOS()
                .stream()
                .collect(Collectors.toMap(ActivitySeckillCommodityRespDTO::getCommodityId, Function.identity()));

        ActivitySeckillTimeRespDTO timeRespVO = activity.getActivitySeckillTimeRespVOS().stream()
                .filter(a -> a.getTimes().equals(times))
                .findFirst().orElse(null);

        if (timeRespVO == null) {
            log.warn("activity:{} time:{} not found", activityId, times);
            return List.of();
        }


        //商品列表
        List<StoreCategoryDTO> storeCategoryDTOS = commodityStoreSpuService.appletGetAllSpu(storeId, ClientType.WX, false);
        List<SeckillSpuVO> list = new ArrayList<>();
        for (StoreCategoryDTO storeCategoryDTO : storeCategoryDTOS) {
            for (SpuDto item : storeCategoryDTO.getItems()) {
                //1.活动未配置这个商品
                if (!collect.containsKey(item.getCommodityId())){
                    continue;
                }

                //2.多规格
                if (item.getSkuList() == null || item.getSkuList().size() > 1){
                    continue;
                }

                //3.随心配
                if (Objects.equals(item.getSetmealType(), 2)){
                    continue;
                }

                //4.可选小料
                if (!CollectionUtils.isEmpty(item.getCommodityCondiments())){
                    continue;
                }

                ActivitySeckillCommodityRespDTO commodityRespVO = collect.get(item.getCommodityId());
                SeckillSpuVO seckillSpuVO = new SeckillSpuVO();
                BeanUtils.copyProperties(item, seckillSpuVO);
                seckillSpuVO.setLimitPerItem(commodityRespVO.getLimitPerItem());
                seckillSpuVO.setSeckillPrice(commodityRespVO.getSeckillPrice());
                seckillSpuVO.setSpuUnderlinedPrice(commodityRespVO.getLinePrice());
                seckillSpuVO.setActivityStock(commodityRespVO.getActivityStock());
                seckillSpuVO.setDiscountStackable(activity.getDiscountStackable());
                seckillSpuVO.setStackableActivitieList(activity.getStackableActivitieList());

                list.add(seckillSpuVO);
            }
        }

        int hour = LocalTime.now().getHour();
        // 进行中场次，重新设置库存
        if (hour >= timeRespVO.getStartTime() && hour <= timeRespVO.getEndTime()) {

            Map<Long, Integer> checkedData = activityApi.seckillStock(storeId, activityId,
                    times, collect.keySet()).getCheckedData();

            for (SeckillSpuVO seckillSpuVO : list) {
                seckillSpuVO.setActivityStock(checkedData.get(seckillSpuVO.getCommodityId()));
            }
        }

        return list;
    }
}
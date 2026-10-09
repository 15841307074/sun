package com.htyoudao.youdao.module.promotion.api.seckill;

import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.module.promotion.api.seckill.DTO.ActivityCommodityDTO;
import com.htyoudao.youdao.module.promotion.service.activitySeckillCommodity.ActivitySeckillCommodityService;
import com.htyoudao.youdao.module.promotion.service.activityStrore.ActivityStoreService;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.annotation.Resource;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.validation.annotation.Validated;

import java.util.List;

import static com.htyoudao.youdao.framework.common.pojo.CommonResult.success;

@DubboService
@Validated
public class SeckillApiImpl implements SeckillApi {

    @Resource
    private ActivitySeckillCommodityService activitySeckillCommodityService;

    @Resource
    private ActivityStoreService activityStoreService;

    @Override
    public CommonResult<Boolean> updateCommodityInfo(ActivityCommodityDTO activityCommodityDTO) {
        activitySeckillCommodityService.updateCommodityInfo(activityCommodityDTO);
        return success(true);
    }

    @Override
    public List<Long> setectStoreByActivityId(Long activityId) {
        return activityStoreService.selectStoreIdsByActivityId(activityId);
    }

}

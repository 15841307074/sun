package com.htyoudao.youdao.module.promotion.api.seckill;

import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.module.promotion.api.activity.DTO.ActivityNjnzDTO;
import com.htyoudao.youdao.module.promotion.api.seckill.DTO.ActivityCommodityDTO;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Collection;
import java.util.List;
import java.util.Map;

@Tag(name = "RPC 服务 - 秒杀")
public interface SeckillApi {

    CommonResult<Boolean> updateCommodityInfo(
            @RequestBody ActivityCommodityDTO activityCommodityDTO
    );
    List<Long> setectStoreByActivityId(
            @Parameter  Long activityId
    );
}

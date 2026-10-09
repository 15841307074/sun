package com.htyoudao.youdao.module.promotion.api.lottery;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

@Tag(name = "RPC 服务 - 抽奖活动")
public interface LotteryApi {

    @Operation(summary = "获取下单类型抽奖活动")
    Map<Long, List<Long>> getLotteryList(@RequestParam("storeId") Long storeId, @RequestParam("memberId") Long memberId);
}


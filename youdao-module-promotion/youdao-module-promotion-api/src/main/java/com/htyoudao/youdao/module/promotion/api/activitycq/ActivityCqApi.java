package com.htyoudao.youdao.module.promotion.api.activitycq;

import com.htyoudao.youdao.module.promotion.api.activityjk.DTO.ActivityJkOrderReqDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "RPC 服务 - 抽签活动")
public interface ActivityCqApi {

    @Operation(summary = "获取下单可完成任务的抽签活动")
    void getActivityCqList(ActivityJkOrderReqDTO reqDTO);
}

package com.htyoudao.youdao.module.promotion.api.activityjk;

import com.htyoudao.youdao.module.promotion.api.activityjk.DTO.ActivityJkOrderReqDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * RPC 服务 - 集卡活动
 */
@Tag(name = "RPC 服务 - 集卡活动")
public interface ActivityJkApi {

    /**
     * 查询本次下单可完成任务的集卡活动。
     * 如果订单满足活动条件，会同步完成一次下单任务。
     *
     * @param reqDTO 下单校验参数
     * @return 本次下单命中的集卡活动列表
     */
    @Operation(summary = "获取下单可完成任务的集卡活动")
   void getActivityJkList(ActivityJkOrderReqDTO reqDTO);
}

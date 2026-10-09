package com.htyoudao.youdao.module.promotion.controller.app.lottery.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 用户抽奖记录查询 vo
 */
@Data
public class LotteryVO {
    /** 同一次抽奖点击及重试使用相同的请求标识；新版抽奖和结果查询时校验。 */
    @Schema(name = "requestId", description = "抽奖请求唯一标识，同一次点击重试保持不变")
    private String requestId;

    /** 活动ID */
    @Schema(name = "lotteryId", description = "活动ID")
    private Long lotteryId;

    /** 会员ID */
    @Schema(name = "memberId", description = "会员ID")
    private Long memberId;
    /** 门店ID */
    @Schema(name = "storeId", description = "门店ID")
    private Long storeId;
    /** 是否免费 0否 1是 */
    @Schema(name = "isFree", description = "是否免费 0否 1是")
    private int isFree;

    /** 消耗积分 */
    @Schema(name = "integral", description = "消耗积分")
    private int integral;

    /**所在城市*/
    @Schema(name = "cityName", description = "所在城市")
    private String  cityName;
}

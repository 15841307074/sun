package com.htyoudao.youdao.module.promotion.controller.app.activityJK.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 集卡活动奖品列表响应
 */
@Data
@Schema(description = "集卡活动奖品列表响应")
public class ActivityJkPrizeListVO {

    @Schema(description = "套卡兑换奖品列表")
    private List<PrizeVO> setPrizeList;

    @Schema(description = "隐藏卡兑换奖品列表")
    private List<PrizeVO> hiddenPrizeList;
}

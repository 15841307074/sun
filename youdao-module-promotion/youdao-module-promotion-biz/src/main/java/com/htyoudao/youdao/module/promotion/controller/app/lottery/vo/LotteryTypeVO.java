package com.htyoudao.youdao.module.promotion.controller.app.lottery.vo;

import com.htyoudao.youdao.module.promotion.dal.dataobject.lottery.LotterySettingsDO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class LotteryTypeVO {

    /**
     */
    private Long id;

    /** 奖品类型 1 优惠卷 2 积分 3 实物 4 无奖品 */
    @Schema(name = "lotteryType", description = "奖品类型 1 优惠卷 2 积分 3 实物 4 无奖品")
    private Integer lotteryType;

    /** 活动名称 */
    @Schema(name = "lotteryTitle", description = "活动名称")
    private String lotteryTitle;
    /** 活动id */
    @Schema(name = "activityId", description = "活动id")
    private Long activityId;

    /** 活动图片 */
    @Schema(name = "activityImgUrl", description = "活动图片")
    private String activityImgUrl;
    public LotteryTypeVO(LotterySettingsDO settings) {
        this.id = settings.getId();
        this.lotteryType = settings.getLotteryType();
        this.lotteryTitle = settings.getShareTitle();
        this.activityImgUrl = settings.getActivityImgUrl();
        this.activityId = settings.getActivityId();
    }
}

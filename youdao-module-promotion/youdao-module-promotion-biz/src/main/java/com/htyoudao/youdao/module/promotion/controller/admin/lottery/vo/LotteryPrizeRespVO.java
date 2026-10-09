package com.htyoudao.youdao.module.promotion.controller.admin.lottery.vo;

import com.htyoudao.youdao.module.promotion.dal.dataobject.lottery.LotteryPrizeDO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LotteryPrizeRespVO {

    private Long id;

    /** 奖品设置id */
    @Schema(name = "lotteryId", description = "奖品设置id")
    private Long lotteryId;

    /** 奖品类型 1 优惠卷 2 积分 3 实物 4 无奖品 */
    @Schema(name = "prizeType", description = "奖品类型 1 优惠卷 2 积分 3 实物 4 无奖品")
    private Integer prizeType;

    /** 奖品名称 */
    @Schema(name = "prizeName", description = "奖品名称")
    private String prizeName;

    /** 奖品数量 */
    @Schema(name = "prizeNum", description = "奖品数量")
    private Integer prizeNum;

    /** 已领取数量 */
    @Schema(name = "remainNum", description = "已领取数量")
    private Integer remainNum;

    /** 奖品价值 */
    @Schema(name = "prizeValue", description = "奖品价值")
    private BigDecimal prizeValue;

    /** 是否多次获取 0 否 1 是 */
    @Schema(name = "isRepeat", description = "是否多次获取 0 否 1 是")
    private Integer isRepeat;

    /** 奖品id(根据类型判断是优惠卷 id 还是商品 id) */
    @Schema(name = "awardId", description = "奖品id(根据类型判断是优惠卷 id 还是商品 id)")
    private Long awardId;

    /** 获取概率 单位% */
    @Schema(name = "probability", description = "获取概率 单位%")
    private BigDecimal probability;

    /** 奖品图片 */
    @Schema(name = "prizeImgUrl", description = "奖品图片")
    private String prizeImgUrl;
    /** 是否为保底商品(必须为 无奖品 必须 存在至少1条) */
    @Schema(name = "isGuarantees", description = "是否为保底商品(必须为 无奖品 必须 存在至少1条)")
    private Integer isGuarantees;
    /*** 可中城市 */
    @Schema(name = "winningCitys", description = "可中城市")
    private String winningCitys;
    public LotteryPrizeRespVO(LotteryPrizeDO prize) {
        this.setId(prize.getId());
        this.setLotteryId(prize.getLotteryId());
        this.setPrizeType(prize.getPrizeType());
        this.setPrizeName(prize.getPrizeName());
        this.setPrizeNum(prize.getPrizeNum());
        this.setRemainNum(prize.getRemainNum());
        this.setPrizeValue(prize.getPrizeValue());
        this.setIsRepeat(prize.getIsRepeat());
        this.setAwardId(prize.getAwardId());
        this.setProbability(prize.getProbability());
        this.setPrizeImgUrl(prize.getPrizeImgUrl());
        this.setIsGuarantees(prize.getIsGuarantees());
        this.setWinningCitys(prize.getWinningCitys());
    }
}

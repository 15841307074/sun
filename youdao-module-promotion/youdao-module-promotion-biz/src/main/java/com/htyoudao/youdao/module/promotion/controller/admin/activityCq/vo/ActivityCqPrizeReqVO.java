package com.htyoudao.youdao.module.promotion.controller.admin.activityCq.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class ActivityCqPrizeReqVO {

    /**
     * 奖品id
     */

    @Schema(description = "奖品id")
    private Long id;

    /**
     * 奖品类型
     */

    @Schema(description = "奖品类型 1 优惠卷 2 积分 3 实物 4 无奖品 5现金红包 6 优惠卷包 7 大奖")
    private Integer prizeType;

    /**
     * 奖品名称
     */

    @Schema(description = "奖品名称")
    private String prizeName;

    /**
     * 奖品数量
     */

    @Schema(description = "奖品数量")
    private Integer prizeNum;

    /**
     * 奖品价值
     */

    @Schema(description = "奖品价值")
    private BigDecimal prizeValue;

    /**
     * 中奖概率
     */

    @Schema(description = "中奖概率")
    private BigDecimal probability;

    /**
     * 奖品图片
     */

    @Schema(description = "奖品图片")
    private String prizeImgUrl;

    /**
     * 奖品业务id
     */

    @Schema(description = "奖品业务id")
    private Long awardId;

    /**
     * 已领取数量
     */

    @Schema(description = "已领取数量")
    private Integer remainNum;

    /**
     * 中奖城市
     */

    @Schema(description = "中奖城市")
    private List<String> winningCitys;

    /**
     * 券名称
     */

    @Schema(description = "券名称")
    private String couponName;

    /**
     * 是否保底
     */

    @Schema(description = "是否保底")
    private Integer isGuarantees;

    /**
     * 保底次数
     */

    @Schema(description = "保底次数")
    private Integer minimumNumber;

}
package com.htyoudao.youdao.module.promotion.controller.app.activityJK.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

/**
 * 奖品信息VO
 *
 * @date 2026-03-14
 */
@Data
@Schema(description = "奖品信息")
public class PrizeVO {

    @Schema(description = "奖品ID")
    private Long id;

    @Schema(description = "奖品名称")
    private String prizeName;

    @Schema(description = "奖品类型 1积分 2优惠券 3优惠券包 4实物 5现金红包")
    private Integer prizeType;

    @Schema(description = "奖品类型名称")
    private String prizeTypeName;

    @Schema(description = "奖品图片")
    private String prizeImgUrl;

    @Schema(description = "奖品价值")
    private BigDecimal prizeValue;

    @Schema(description = "总库存")
    private Integer totalStock;

    @Schema(description = "剩余库存")
    private Integer remainStock;

    @Schema(description = "已兑换数量")
    private Integer exchangedCount;

    @Schema(description = "兑换类型 1套系卡兑换 2隐藏卡兑换")
    private Integer exchangeType;

    @Schema(description = "需要卡片数量")
    private Integer needCardCount;

    @Schema(description = "需要的卡片ID列表")
    private List<Long> needCardIds;

    @Schema(description = "每人限兑数量 0不限制")
    private Integer exchangeLimit;

    @Schema(description = "用户是否可兑换")
    private Boolean userCanExchange;

    @Schema(description = "用户已兑换数量")
    private Integer userExchangeCount;

    @Schema(description = "兑换说明")
    private String exchangeDesc;
}

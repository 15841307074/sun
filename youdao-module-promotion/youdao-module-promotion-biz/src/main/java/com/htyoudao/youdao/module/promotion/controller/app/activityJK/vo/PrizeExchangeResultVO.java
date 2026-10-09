package com.htyoudao.youdao.module.promotion.controller.app.activityJK.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 奖品兑换结果响应VO
 *
 * @date 2026-03-14
 */
@Data
@Schema(description = "奖品兑换结果响应")
public class PrizeExchangeResultVO {


    @Schema(description = "兑换记录ID")
    private Long id;

    @Schema(description = "奖品名称")
    private String prizeName;

    @Schema(description = "奖品类型 1积分 2优惠券 3优惠券包 4实物 5现金红包")
    private Integer prizeType;

    @Schema(description = "奖品有效期")
    private Date expireTime;

    @Schema(description = "优惠券码（优惠券类型）")
    private String couponCode;

    @Schema(description = "红包金额（红包类型）")
    private BigDecimal redPacketAmount;

    @Schema(description = "红包领取参数（红包类型）")
    private String packageInfo;

    @Schema(description = "商户转账单号（红包类型）")
    private String outBillNo;

    @Schema(description = "积分数量（积分类型）")
    private Integer pointAmount;

    @Schema(name = "prizeImgUrl", description = "奖品图片")
    private String prizeImgUrl;
    @Schema(description = "奖品id")
    private Long awardId;
    @Schema(description = "用户优惠卷id")
    private Long userCouponId;
}

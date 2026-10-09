package com.htyoudao.youdao.module.promotion.controller.app.activityJK.vo;

import com.htyoudao.youdao.framework.mybatis.core.dataobject.BusinessBaseDO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 兑换记录VO
 *
 * @date 2026-03-14
 */
@Data
@Schema(description = "兑换记录")
public class ExchangeRecordVO extends BusinessBaseDO implements Serializable {

    @Schema(description = "记录ID")
    private Long id;

    @Schema(description = "奖品ID")
    private Long prizeId;

    @Schema(description = "奖品名称")
    private String prizeName;

    @Schema(description = "奖品类型")
    private Integer prizeType;

    @Schema(description = "奖品图片")
    private String prizeImgUrl;

    @Schema(description = "兑换时间")
    private Date exchangeTime;

    @Schema(description = "奖品状态 0已发放 1待发货 2已发货 3已签收")
    private Integer prizeStatus;

    @Schema(description = "物流单号")
    private String trackingNumber;
    @Schema(name = "claimStatus", description = "红包领取状态(1 未领取  2 已领取  3已过期)")
    private Integer claimStatus;

    @Schema(description = "奖品id")
    private Long awardId;

    @Schema(description = "用户优惠卷id")
    private Long userCouponId;
    @Schema(name = "storeId", description = "商户转账单号")
    private String outBillNo;

    @Schema(name = "微信红包返参链接", description = "微信红包返参链接")
    private String packageInfo;
}

package com.htyoudao.youdao.module.promotion.controller.app.activitySign.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "小程序 - 我的签到奖励明细 Response VO")
@Data
public class AppActivitySignMyRewardItemRespVO {

    @Schema(description = "奖励发放记录ID")
    private Long rewardRecordId;

    @Schema(description = "奖品ID/奖励配置ID")
    private Long prizeId;

    @Schema(description = "奖品类型，复用ActivityCqPrizeTypeEnum：1优惠券 2积分 3实物 5现金红包 6优惠券包")
    private Integer prizeType;

    @Schema(description = "奖品内容/奖励名称")
    private String prizeContent;

    @Schema(description = "奖品图片")
    private String prizeImgUrl;

    @Schema(description = "奖品值：积分数/红包金额等")
    private BigDecimal prizeValue;

    @Schema(description = "发放状态：0待发放 1发放中 2已发放 3发放失败")
    private Integer issueStatus;

    @Schema(description = "发放时间")
    private LocalDateTime issueTime;

    @Schema(description = "红包领取状态：1未领取 2已领取 3已过期/已失效")
    private Integer claimStatus;

    @Schema(description = "商户转账单号/红包外部单号，非红包为空")
    private String outBillNo;

    @Schema(description = "红包领取凭证/领取信息，非红包为空；前端使用该字段调微信领取")
    private String packageInfo;

    @Schema(description = "实物奖品状态：1填写地址 2待发货 3待收货 9超时未填写；非实物为空或0")
    private Integer prizeState;

}

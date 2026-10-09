package com.htyoudao.youdao.module.promotion.controller.app.activityAnswer.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 有奖问答我的奖励响应。
 */
@Data
public class ActivityAnswerMyRewardRespVO {

    @Schema(description = "奖励记录ID")
    private Long rewardLogId;

    @Schema(description = "答题记录ID")
    private Long recordId;

    @Schema(description = "答题编号")
    private String answerNo;

    @Schema(description = "活动ID")
    private Long activityId;

    @Schema(description = "活动类型，9有奖问答")
    private Integer activityType;

    @Schema(description = "活动标题")
    private String answerTitle;

    @Schema(description = "奖品类型")
    private Integer prizeType;

    @Schema(description = "奖品ID")
    private Long prizeId;

    @Schema(description = "奖品名称")
    private String prizeName;

    @Schema(description = "奖品图片")
    private String prizeImgUrl;

    @Schema(description = "奖品价值")
    private BigDecimal prizeValue;

    @Schema(description = "红包领取状态")
    private Integer claimStatus;

    @Schema(description = "红包调起支付参数")
    private String packageInfo;

    @Schema(description = "红包商户转账单号")
    private String outBillNo;

    @Schema(description = "奖品状态")
    private Integer prizeState;

    @Schema(description = "收货人")
    private String receiveUser;

    @Schema(description = "收货手机号")
    private String receiveMobile;

    @Schema(description = "收货地址")
    private String receiveAddress;

    @Schema(description = "快递公司")
    private String expressCompany;

    @Schema(description = "快递单号")
    private String trackingNumber;

    @Schema(description = "发放时间")
    private LocalDateTime grantTime;
}

package com.htyoudao.youdao.module.promotion.controller.app.activityVote.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class AppVoteMyRewardRespVO {

    @Schema(description = "记录id")
    private Long id;

    @Schema(description = "活动id")
    private Long activityId;

    @Schema(description = "奖品类型 1积分 2优惠券 3优惠券包 4实物奖品 5现金红包")
    private Integer prizeType;

    @Schema(description = "奖品名称")
    private String prizeName;

    @Schema(description = "奖品图片")
    private String prizeImgUrl;

    @Schema(description = "奖品价值")
    private BigDecimal prizeValue;

    @Schema(description = "红包领取状态 1未领取 2已领取 3已失效")
    private Integer claimStatus;

    @Schema(description = "红包调起支付参数")
    private String packageInfo;

    @Schema(description = "红包商户转账单号")
    private String outBillNo;

    @Schema(description = "奖品状态 0已发放 1未填写地址 2待发货 3已发货 9退回")
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
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime grantTime;
}

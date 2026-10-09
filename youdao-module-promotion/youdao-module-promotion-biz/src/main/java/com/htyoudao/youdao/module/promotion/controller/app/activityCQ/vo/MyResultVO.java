package com.htyoudao.youdao.module.promotion.controller.app.activityCQ.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "我的抽签结果")
public class MyResultVO {

    @Schema(description = "记录ID")
    private Long id;

    @Schema(description = "签码")
    private String signCode;

    @Schema(description = "结果状态")
    private Integer resultStatus;

    @Schema(description = "奖品ID")
    private Long prizeId;

    @Schema(description = "奖品类型")
    private Integer prizeType;

    @Schema(description = "奖品内容")
    private String prizeContent;

    @Schema(description = "奖品图片")
    private String prizeImgUrl;

    @Schema(description = "奖品状态")
    private Integer prizeState;

    @Schema(description = "快递单号")
    private String trackingNumber;

    @Schema(description = "红包拉起参数")
    private String packageInfo;

    @Schema(description = "红包商户单号")
    private String outBillNo;

    @Schema(description = "红包领取状态(1 未领取 2 已领取 3 已过期)")
    private Integer claimStatus;

    @Schema(description = "获取抽签时间")
    private LocalDateTime drawTime;
}

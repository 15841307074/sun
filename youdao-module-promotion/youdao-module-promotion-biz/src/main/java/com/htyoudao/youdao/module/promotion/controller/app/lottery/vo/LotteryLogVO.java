package com.htyoudao.youdao.module.promotion.controller.app.lottery.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;

/**
 * 用户抽奖记录查询 vo
 */
@Data
public class LotteryLogVO {
    @Schema(name = "id", description = "logId")
    private Long id;
    /**
     * 活动设置id
     */
    @Schema(name = "lotteryId", description = "活动设置id")
    private Long lotteryId;
    /**
     * 会员ID
     */
    @Schema(name = "memberId", description = "会员ID")
    private Long memberId;
    /**
     * 收货地址
     */
    @Schema(name = "receiveAddress", description = "收货地址")
    private String receiveAddress;
        /** 奖品类型 1 优惠卷 2 积分 3 实物 4 无奖品 */
    @Schema(name = "prizeType", description = "奖品类型 1 优惠卷 2 积分 3 实物 4 无奖品")
    private Integer prizeType;
    /**
     * 收件人
     */
    @Schema(name = "receiveUser", description = "收件人")
    private String receiveUser;
    /**
     * 联系电话
     */
    @Schema(name = "receiveMobile", description = "联系电话")
    private String receiveMobile;

    /**
     * 电话
     */
    @Schema(name = "memberMobile", description = "电话")
    private String memberMobile;

    /**
     * 活动类型
     */
    @Schema(name = "lotteryType", description = "活动类型")
    private Integer lotteryType;
    /**
     * 活动开始时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    @Schema(name = "lotteryStartDate", description = "活动开始时间")
    private Date lotteryStartDate;
    /**
     * 活动结束时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    @Schema(name = "lotteryEndDate", description = "活动结束时间")
    private Date lotteryEndDate;

      /** 快递公司 */
    @Schema(name = "expressCompany", description = "快递公司")
    private String expressCompany;

     /** 快递单号 */
    @Schema(name = "trackingNumber", description = "快递单号")
    private String trackingNumber;

    /** 快递单号 */
    @Schema(name = "memberNickName", description = "收获人")
    private String memberNickName;



}

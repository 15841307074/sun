package com.htyoudao.youdao.module.promotion.controller.admin.lottery.vo;

import com.alibaba.fastjson2.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.htyoudao.youdao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LotteryLogExportVO extends PageParam {
    @Schema(name = "id", description = "logId")
    private Long id;
    /**
     * 活动设置id
     */
    @Schema(name = "lotteryId", description = "活动设置id")
    @NotNull(message = "活动ID不能为空")
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
     * 会员名称
     */
    @Schema(name = "memberName", description = "会员名称")
    private String memberName;


    /**
     * 活动类型
     */
    @Schema(name = "lotteryType", description = "活动类型")
    private Integer lotteryType;
//    /**
//     * 活动开始时间
//     */
//    @Schema(name = "lotteryStartDate", description = "活动开始时间")
//    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
//    private LocalDateTime lotteryStartDate;
//    /**
//     * 活动结束时间
//     */
//    @Schema(name = "lotteryEndDate", description = "活动结束时间")
//    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
//    private LocalDateTime lotteryEndDate;


    @Schema(description = "开始日期")
    private String lotteryStartDate;

    @Schema(description = "结束日期")
    private String lotteryEndDate;
    

    /** 快递公司 */
    @Schema(name = "expressCompany", description = "快递公司")
    private String expressCompany;

    /** 快递单号 */
    @Schema(name = "trackingNumber", description = "快递单号")
    private String trackingNumber;

    /** 快递单号 */
    @Schema(name = "memberNickName", description = "收货人")
    private String memberNickName;


    @Schema(name = "receiveAddressType",description = "收货地址(0 未填  1 已填)")
    private Integer receiveAddressType;


    @Schema(name = "trackingType",description = "物流单号(0 未填  1 已填)")
    private Integer trackingType;

    @Schema(name = "claimStatus", description = "红包领取状态(1 未领取  2 已领取  3已过期)")
    private Integer claimStatus;
}

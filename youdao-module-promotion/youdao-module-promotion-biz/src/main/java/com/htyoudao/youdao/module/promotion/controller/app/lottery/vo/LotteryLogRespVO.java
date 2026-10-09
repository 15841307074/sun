package com.htyoudao.youdao.module.promotion.controller.app.lottery.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

@Data
public class LotteryLogRespVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;
    /**
     * 奖品id
     */

    private Long lotteryPrizeId;
    /** 奖品类型 1 优惠卷 2 积分 3 实物 4 无奖品 */
    @Schema(name = "prizeType", description = "奖品类型 1 优惠卷 2 积分 3 实物 4 无奖品")
    private Integer prizeType;

    /** 奖品价值 */
    @Schema(name = "prizeValue", description = "奖品价值")
    private BigDecimal prizeValue;

    /** 奖品名称 */
    @Schema(name = "prizeName", description = "奖品名称")
    private String prizeName;

    /** 奖品图片 */
    @Schema(name = "prizeImgUrl", description = "奖品图片")
    private String prizeImgUrl;

    /** 会员ID */
    @Schema(name = "memberId", description = "会员ID")
    private Long memberId;

    /** 快递单号 */
    @Schema(name = "trackingNumber", description = "快递单号")
    private String trackingNumber;

    /** 会员手机号 */
    @Schema(name = "memberMobile", description = "会员手机号")
    private String memberMobile;

    /** 快递公司 */
    @Schema(name = "expressCompany", description = "快递公司")
    private String expressCompany;

    /** 收货地址 */
    @Schema(name = "receiveAddress", description = "收货地址")
    private String receiveAddress;

    /** 收件人 */
    @Schema(name = "receiveUser", description = "收件人")
    private String receiveUser;

    /** 联系电话 */
    @Schema(name = "receiveMobile", description = "联系电话")
    private String receiveMobile;

    /** 活动设置id */
    @Schema(name = "lotteryId", description = "活动设置id")
    private Long lotteryId;

    /** 0 已发放   商品类型  0未填写收货地址  1 已填写地址 待发货  2已发货 */
    @Schema(name = "prizeState", description = "0 已发放   商品类型  1未填写收货地址  2 已填写地址 待发货  3已发货  9 退回 ")
    private Integer prizeState;
    /** 活动类型 */
    @Schema(name = "lotteryType", description = "活动类型")
    private int lotteryType;
    /** 活动开始时间 */
    @Schema(name = "lotteryStartTime", description = "活动开始时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date lotteryStartTime;

    /** 活动结束时间 */
    @Schema(name = "lotteryEndTime", description = "活动结束时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date lotteryEndTime;
}

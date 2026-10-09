package com.htyoudao.youdao.module.promotion.controller.admin.usercoupon.VO;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;

@Data
public class UserCouponInfoRespVo {

    private Long id;


    @Schema(description = "会员名称")
    private String memberName;


    @Schema(description = "会员手机号")
    private String memberMobile;


    @Schema(description = "优惠券状态(0-待使用,1-已使用,2-未开始,3-进行中,4-已到期)")
    private Integer couponStatus;


    @Schema(description = "优惠卷领取时间")
    private LocalDateTime couponCreateTime;


    @Schema(description = "优惠券有效开始时间")
    private Date couponStartTime;


    @Schema(description = "优惠券有效结束时间")
    private Date couponEndTime;

    @Schema(description = "使用时间信息，根据use_type而定")
    private String couponUseTime;


    @Schema(description = "支付金额")
    private BigDecimal payAmount;




}

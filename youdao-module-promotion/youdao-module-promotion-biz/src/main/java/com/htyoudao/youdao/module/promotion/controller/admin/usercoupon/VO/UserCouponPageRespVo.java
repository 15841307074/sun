package com.htyoudao.youdao.module.promotion.controller.admin.usercoupon.VO;

import com.alibaba.fastjson2.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;

@Data
public class UserCouponPageRespVo {

    private Long id;


    @Schema(description = "会员名称")
    private String memberName;


    @Schema(description = "会员手机号")
    private String memberMobile;


    @Schema(description = "优惠券状态(0-待使用,1-已使用,2-未开始,3-进行中,4-已到期)")
    private Integer couponStatus;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "优惠卷领取时间")
    private Date couponCreateTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "优惠券有效开始时间")
    private Date couponStartTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "优惠券有效结束时间")
    private Date couponEndTime;

    @Schema(description = "使用时间信息，根据use_type而定")
    private String couponUseTime;


    @Schema(description = "支付金额")
    private BigDecimal payAmount;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "到期时间")
    private Date expirationTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "有效开始日期")
    private Date vaildStartTime;

    @Schema(description = "0 小程序链接领券 1 H5链接领券 2 短信链接领券 3 小程序内部领券（banner）4 小程序内部领券（弹窗）5 小程序内部领券（分享）6 推广 7积分商城  999默认", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer couponSource;

    private String storeName;

    @Schema(description = "0 无 1 新注册用户 2 老用户 3 回归用户", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer userRestrictions;

    @Schema(description = "使用时间", requiredMode = Schema.RequiredMode.REQUIRED)
    //@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
    private String useTime;
}

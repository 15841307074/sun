package com.htyoudao.youdao.module.promotion.controller.admin.couponpackage.vo;

import com.alibaba.fastjson2.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;

@Data
public class CouponPackageCollectRespVO {

    private Long id;


    @Schema(description = "会员名称")
    private String memberName;


    @Schema(description = "会员手机号")
    private String memberMobile;


    @Schema(description = "优惠卷领取时间")
    private LocalDateTime createTime;



}

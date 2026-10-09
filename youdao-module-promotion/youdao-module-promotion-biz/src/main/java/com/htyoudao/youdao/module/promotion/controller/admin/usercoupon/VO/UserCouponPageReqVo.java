package com.htyoudao.youdao.module.promotion.controller.admin.usercoupon.VO;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.htyoudao.youdao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;


@Data
public class UserCouponPageReqVo extends PageParam {

    private static final Integer PAGE_NUM = 1;

    @Schema(description = "优惠卷ID")
    private Long couponId;

    @Schema(description = "会员名称/手机号")
    private String memberName;

    @Schema(description = "优惠券状态(0-待使用,1-已使用,2-未开始,3-进行中,4-已到期)")
    private Integer couponStatus;

    @Schema(description = "0 小程序链接领券 1 H5链接领券 2 短信链接领券 3 小程序内部领券（banner）4 小程序内部领券（弹窗）5 小程序内部领券（分享）6 推广 7积分商城  999默认", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer couponSource;

    @Schema(description = "页码，从 1 开始", requiredMode = Schema.RequiredMode.REQUIRED,example = "1")
    @NotNull(message = "页码不能为空")
    @Min(value = 1, message = "页码最小值为 1")
    private Integer pageNum = PAGE_NUM;

    @Schema(description = "0 无 1 新注册用户 2 老用户 3 回归用户", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer userRestrictions;

    @Schema(description = "门店id")
    private Long storeId;

    @Schema(description = "orgId")
    private Long orgId;

    @Schema(description = "使用开始时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime useStartTime;

    @Schema(description = "使用结束时间")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime useEndTime;


    @Schema(description = "领取结束时间")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime couponCreateEndTime;

    @Schema(description = "领取开始时间")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime couponCreateStartTime;
}

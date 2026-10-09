package com.htyoudao.youdao.module.promotion.controller.admin.advertising.carousel.VO;

import com.alibaba.excel.annotation.ExcelProperty;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;

@Schema(description = "管理后台 - 轮播优惠券选择 Response VO")
@Data
public class CouponChooseRespVO {

    @Schema(description = "优惠券ID")
    private String id;

    @Schema(description = "优惠券名称")
    private String couponName;

    @Schema(description = "优惠券备注")
    private String remark;

    @Schema(description = "适用区域 1 全部可用 2 部分门店可用")
    private Integer isCommon;

    @Schema(description = "使用商品 1 通用 2指定商品可用 3指定商品不可用")
    private Integer isCommonStore;

    @Schema(description = "优惠券类型 0 满减券 1 折扣券")
    private Integer couponType;

    @Schema(description = "领取限制 0 不限制 1 新注册用户 2 老用户 3 回归用户")
    private Integer userRestrictions;

    @Schema(description = "用餐方式 0 全部可用 1 堂食可用 2 外卖可用")
    private Integer habit;

    @Schema(description = "减免/折扣")
    private BigDecimal reliefOrDiscount;

    @Schema(description = "使用门槛类型 （0 无门槛 1 价格门槛 2 商品件数门槛）")
    private Integer doorsillType;

    @Schema(description = "门槛金额/件数")
    private Integer doorsill;


    /**
     * 优惠券有效开始时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    @Schema(description = "优惠券有效开始时间")
    private LocalDateTime couponStartTime;

    /**
     * 优惠券有效结束时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    @Schema(description = "优惠券有效结束时间")
    private LocalDateTime couponEndTime;

    @Schema(description = "使用类型 0 时间段 1 立即生效 2 领取N天后生效")
    private Integer useType;

    @Schema(description = "使用时间信息，根据use_type而定")
    private String useTime;

    @Schema(description = "折扣")
    private Integer discount;

    @Schema(description = "满减金额")
    private BigDecimal reduceAmount;

    @Schema(description = "满减金额")
    private Integer fullReduction;
}

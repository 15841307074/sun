package com.htyoudao.youdao.module.order.controller.admin.order.vo;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.htyoudao.youdao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serial;
import java.time.LocalDateTime;
import java.util.Set;

import static com.htyoudao.youdao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Data
public class BzOrderReqVO extends PageParam {

    @Serial
    private static final long serialVersionUID = -8384948804635255123L;
    /**
     * 商家id
     */
    private Long storeId;

    /**
     * 商家名称
     */
    private String storeName;

    /**
     * 商家id集合
     */
    private Set<Long> storeIdList;

    /**
     * 商家id集合
     */
    private Set<Long> storeIds;

    @Schema(description = "订单号")
    private String orderSn;

    @Schema(description = "订单状态：0-已取消；10-未付款订单；20-已付款；30-待取餐；40-待配送；50-配送中；60-已完成；80-制作中；110-待接单；120-已接单；200-待取餐")
    private Integer orderState;

    @JsonIgnore
    private Boolean includeAcceptedErrandWhenMaking;

    @Schema(description = "查询历史订单")
    private Integer orderStateHistory;

    @Schema(description = "查询进行订单")
    private Integer orderStateProcess;

    @Schema(description = "商品名称")
    private String goodsName;

    @Schema(description = "支付方式名称")
    private String paymentName;

    @Schema(description = "支付方式code 0现金 1 微信 2支付宝")
    private String paymentCode;

    @Schema(description = "订单来源 1-微信小程序")
    private Integer orderFrom;

    @Schema(description = "买家ID")
    private Long memberId;

    @Schema(description = "openId")
    private String openId;

    @Schema(description = "门店电话")
    private String storePhone;

    @Schema(description = "创建时间", example = "[2022-07-01 00:00:00,2022-07-01 23:59:59]")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime startTime;

    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime endTime;

    @Schema(description = "第三方单号")
    private String expressNumber;

    @Schema(description = "是否为售后订单 1-是 0-不是")
    private Integer isAfterSales;

    @Schema(description = "订单类型")
    private Integer orderType;

    @Schema(description = "PC端类型")
    private Integer orderPcType;

    private Integer attribute;
}

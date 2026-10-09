package com.htyoudao.youdao.module.order.controller.admin.order.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.htyoudao.youdao.module.order.dal.es.BzOrderDocument;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Schema(description = "管理后台 - 订单分页 Request VO")
@Data
public class OrderResVO {

    @Schema(description = "订单号", example = "1")
    private String orderSn;

    @Schema(description = "手机号 模糊", example = "1")
    private String takeAwayTel;

    @Schema(description = "跑腿员电话")
    private String deliveryPhone;

    @Schema(description = "跑腿员姓名")
    private String deliveryName;

    @Schema(description = "门店名称", example = "youdao")
    private String storeName;

    @Schema(description = "订单类型 0 堂食 1 打包 2 外卖 3 预订单", example = "1")
    private Integer orderType;

    @Schema(description = "订单渠道 1-微信小程序", example = "1")
    private Integer orderFrom;

    @Schema(description = "订单状态", example = "1")
    private Integer orderState;

    @Schema(description = "商品数量", example = "1")
    private Long goodsNum;

    @Schema(description = "支付方式code 0现金 1 微信 2支付宝", example = "1")
    private String paymentCode;

    @Schema(description = "创建时间", example = "1")
    private String createTime;

    @Schema(description = "取餐号", example = "1")
    private String pickUpNum;

    @Schema(description = " 活动优惠总金额 （= 店铺优惠券 + 平台优惠券 + 活动优惠【店铺活动 + 平台活动】 + 积分抵扣金额）", example = "1")
    private BigDecimal activityDiscountAmount;

    @Schema(description = "订单总金额", example = "1")
    private BigDecimal payAmount;

    @Schema(description = "是否是秒杀单，0否 1是", example = "0")
    private Integer lockState;

    @Schema(description = "跑腿员会员ID，对应 wx_member.id")
    private Long deliveryId;

    @Schema(description = "用户支付跑腿赏金")
    private BigDecimal errandRewardAmount;

    @Schema(description = "门店跑腿补贴金额")
    private BigDecimal errandStoreSubsidyAmount;

    @Schema(description = "跑腿员性别限制：0不限 1男 2女")
    private Integer errandGenderLimit;

    private Long businessId;

    @Schema(description = "商品图片", example = "[http://xxx.jpg]")
    private Set<String> imageList = new HashSet<>();
}

package com.htyoudao.youdao.module.order.controller.admin.order.vo;


import com.htyoudao.youdao.module.order.dal.dataobject.order.BzOrderProductSonDO;
import com.htyoudao.youdao.module.order.dal.dataobject.order.BzOrderPurchaseDO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


@Data
public class BzOrderAppListRspVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1899630291968363378L;

    @Schema(description = "订单号", example = "ORD202505071633545702035")
    private String orderSn;

    @Schema(description = "取餐码", example = "A01")
    private String pickUpNum;

    @Schema(description = "商家名称", example = "沈阳师范2大学")
    private String storeName;

    @Schema(description = "创建时间", example = "2025-05-05 00:00:00")
    private String createTime;

    @Schema(description = "订单状态：0-已取消；10-未付款订单；20-已付款；30-待取餐；40-代配送 50-已配送 60-已完成;", example = "0 进行中， 1 历史")
    private Integer orderState;

    @Schema(description = "订单状态描述", example = "已取消")
    private String orderStateName;

    @Schema(description = "跑腿员电话")
    private String deliveryPhone;

    @Schema(description = "跑腿员姓名")
    private String deliveryName;

    @Schema(description = "用户支付跑腿赏金")
    private BigDecimal errandRewardAmount;

    @Schema(description = "门店跑腿补贴金额")
    private BigDecimal errandStoreSubsidyAmount;

    @Schema(description = "支付金额", example = "12.00")
    private BigDecimal payAmount;

    @Schema(description = "门店电话", example = "13462736475")
    private String storePhone;

    @Schema(description = "商品数量", example = "2")
    private Long goodsNum;

    @Schema(description = "订单类型描述", example = "堂食")
    private String orderTypeName;

    @Schema(description = "订单类型 订单类型：0 堂食 1 打包 2 外卖 3代取", example = "0")
    private Integer orderType;

    @Schema(description = "订单来源 0点餐机 1微信小程序 2支付宝")
    private Integer orderFrom;

    @Schema(description = "是否是秒杀单，0否 1是", example = "0")
    private Integer lockState;

    @Schema(description = "商品图片", example = "[http://xxx.jpg]")
    private Set<String> imageList = new HashSet<>();
}

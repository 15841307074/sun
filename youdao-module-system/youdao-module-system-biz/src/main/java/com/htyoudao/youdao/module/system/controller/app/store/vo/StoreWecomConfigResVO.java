package com.htyoudao.youdao.module.system.controller.app.store.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class StoreWecomConfigResVO {
    @Schema(description = "门店id", example = "1024")
    private Long storeId;
    @Schema(description = "门店名称", example = "1024")
    private String storeName;
    @Schema(description = "门店地址", example = "1024")
    private String storeAddress;
    @Schema(description = "门店经度", example = "1024")
    private double longitude;
    @Schema(description = "门店维度", example = "1024")
    private double latitude;
    @Schema(description = "店铺距离", example = "1024")
    private double distance;
    @Schema(description = "企微二维码", example = "1024")
    private String qrCode;
    @Schema(description = "营业状态  0 正常营业  1 休息", example = "1024")
    private Integer openStatus;
    @Schema(description = "门店是否支持外卖（0支持，1不支持）", example = "1024")
    private Integer storeTakeaway;
    @Schema(description = "门店营业时间")
    private String  storeHours;
    @Schema(description = "门店公告")
    private String storeAnnouncement;
    @Schema(description = "门店电话")
    private String storePhone;
    @Schema(description = "配送费")
    private BigDecimal additionaaCosts;
    @Schema(description = "打包费")
    private BigDecimal packCosts;
    @Schema(description = "门店收款方式（0 二维码，1 现金，3 待定）")
    private String storePayType;
    @Schema(description = "门店是否支持不付款下单 （0支持 1 不支持）")
    private Integer storeWithoutPayment;
    @Schema(description = "校园配送开关：0支持 1不支持")
    private Integer campusDeliveryStatus;
    @Schema(description = "校园配送补贴")
    private BigDecimal campusDeliverySubsidy;
    @Schema(description = "代取起送费")
    private BigDecimal campusMinimumDeliveryFee;
    @Schema(description = "校园配送费不再配置，固定返回0")
    private BigDecimal campusDeliveryFee;
    @Schema(description = "校园配送费计算方式不再配置，固定返回null")
    private Integer campusDeliveryCalculationType;
    @Schema(description = "起送费/打包费限额")
    private BigDecimal minimumDeliveryFee;
    @Schema(description = "费用计算方式  0 按商品  1 按订单  （ 费用类型 2 外卖配送费不考虑该字段）")
    private Integer storeCalculationType;
    @Schema(description = "企微二维码类型 0 福利官 1社群", example = "1024")
    private Integer qrType;
    @Schema(description = "配送费")
    private BigDecimal minimumPackageFee;
    @Schema(description = "外卖打包费限额")
    private BigDecimal minimumDeliveryPackFee;
    @Schema(description = "外卖费用计算方式  0 按商品  1 按订单  （ 费用类型 2 外卖配送费不考虑该字段）")
    private Integer storeDeliveryCalculationType;
    @Schema(description = "外卖打包费")
    private BigDecimal packDeliveryCosts;
    @Schema(description = "抖音id")
    private Long tiktokId;
    @Schema(description = "外卖时间")
    private String deliveryTime;
    @Schema(description = "是否弹出提示窗  0 否 1是")
    private Integer isPrompt;
    @Schema(description = "弹窗提示文案")
    private String promptText;
    @Schema(description = "所在城市")
    private String cityName;
    @Schema(description = "门店背景图片")
    private String storeBackgroundImage;
}

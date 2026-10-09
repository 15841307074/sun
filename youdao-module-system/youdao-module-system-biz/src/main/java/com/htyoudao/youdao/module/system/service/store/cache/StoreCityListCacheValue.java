package com.htyoudao.youdao.module.system.service.store.cache;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 小程序城市门店列表缓存值。
 *
 * <p>仅保存可复用的静态数据，距离和当前营业状态在请求时实时计算。</p>
 */
@Data
public class StoreCityListCacheValue {

    /** 门店 ID。 */
    private Long storeId;
    /** 门店名称。 */
    private String storeName;
    /** 门店地址。 */
    private String storeAddress;
    /** 门店经度。 */
    private Double longitude;
    /** 门店纬度。 */
    private Double latitude;
    /** 企微二维码。 */
    private String qrCode;
    /** 企微二维码类型。 */
    private Integer qrType;
    /** 门店基础营业状态。 */
    private Integer openStatus;
    /** 是否支持外卖。 */
    private Integer storeTakeaway;
    /** 营业时间。 */
    private String storeHours;
    /** 门店公告。 */
    private String storeAnnouncement;
    /** 门店电话。 */
    private String storePhone;
    /** 外卖配送费。 */
    private BigDecimal additionaaCosts;
    /** 起送费。 */
    private BigDecimal minimumDeliveryFee;
    /** 堂食或外带打包费。 */
    private BigDecimal packCosts;
    /** 堂食或外带打包费限额。 */
    private BigDecimal minimumPackageFee;
    /** 堂食或外带费用计算方式。 */
    private Integer storeCalculationType;
    /** 外卖打包费。 */
    private BigDecimal packDeliveryCosts;
    /** 外卖打包费限额。 */
    private BigDecimal minimumDeliveryPackFee;
    /** 外卖打包费计算方式。 */
    private Integer storeDeliveryCalculationType;
    /** 门店收款方式。 */
    private String storePayType;
    /** 是否支持不付款下单。 */
    private Integer storeWithoutPayment;
    /** 校园配送开关。 */
    private Integer campusDeliveryStatus;
    /** 校园配送补贴。 */
    private BigDecimal campusDeliverySubsidy;
    /** 校园配送起送费。 */
    private BigDecimal campusMinimumDeliveryFee;
    /** 校园配送费，当前固定为零。 */
    private BigDecimal campusDeliveryFee;
    /** 校园配送费计算方式，当前固定为空。 */
    private Integer campusDeliveryCalculationType;
    /** 抖音门店 ID。 */
    private Long tiktokId;
    /** 外卖时间。 */
    private String deliveryTime;
    /** 是否显示提示弹窗。 */
    private Integer isPrompt;
    /** 提示弹窗文案。 */
    private String promptText;
    /** 门店所在城市。 */
    private String cityName;
}

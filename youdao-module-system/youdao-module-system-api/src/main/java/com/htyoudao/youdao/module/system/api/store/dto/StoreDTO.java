package com.htyoudao.youdao.module.system.api.store.dto;

import com.htyoudao.youdao.framework.common.exception.ErrorCode;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * <p>
 * 门店基础信息
 * </p>
 *
 * @author zhangjihe
 * @since 2025-04-30
 */
@Data
public class StoreDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 7469094675732731962L;

    /**
     * 门店id
     */
    private Long storeId;

    /**
     * 门店名称
     */
    private String storeName;

    /**
     * 门店电话
     */
    private String storePhone;

    /**
     * 门店营业时间
     */
    private String  storeHours;

    /**
     * 外卖时间
     */
    private String deliveryTime;

    /**
     * 营业状态  0 正常营业  1 休息
     */
    private Integer openStatus;

    /**
     * 门店是否支持外卖（0支持，1不支持）
     */
    private Integer storeTakeaway;

    /**
     * 门店状态（0 正常营业 1 闭店）
     */
    private Integer storeStatus;

    /**
     * 门店是否支持不付款下单 （0支持 1 不支持）
     */
    private Integer storeWithoutPayment;

    /**
     * 校园配送状态（0开启，1关闭）
     */
    private Integer campusDeliveryStatus;

    /**
     * 校园配送补贴
     */
    private BigDecimal campusDeliverySubsidy;

    /**
     * 小程序门店状态（0 正常营业 1  闭店） 废弃
     */
    private Integer miniproStatus;

    /**
     * 点餐方式 0 全部 1 点餐机  2 小程序
     */
    private Integer orderStoreType;

    /**
     * 配送员
     */
    private String deliveryName;

    /**
     * 配送员电话
     */
    private String deliveryPhone;

    /**
     * 终端号
     */
    private String terminalSn;

    /**
     * 代取分账商户号
     */
    private String terminalKey;

    /**
     * 门店纬度
     */
    private Double storeLatitude;

    /**
     * 门店经度
     */
    private Double storeLongitude;

    /**
     * 省份
     */
    private String province;

    /**
     * 城市
     */
    private String city;

    /**
     * 区域
     */
    private String area;

    /**
     * 小票模板
     */
    private String printerTemplate;

    /**
     * 配送费/打包费
     */
    private List<StoreExpensesVO> expensesList;

    /**
     * 门店地址
     */
    private String storeAddress;

    /**
     * 高峰时段
     */
    private String peakHours;

    /**
     * 出餐时间
     */
    private String mealTime;

    private ErrorCode errorCode;

    @Data
    public static class StoreExpensesVO implements Serializable{
        @Serial
        private static final long serialVersionUID = 4567730261194789766L;

        /**
         * 费用类型 0 堂食/外带打包费 1 外卖打包费  2 外卖配送费
         */
        private Integer storeExpensesType;

        /**
         * 起送费/打包费限额
         */
        private BigDecimal minimumDeliveryFee;

        /**
         * 配送费/打包费
         */
        private BigDecimal additionaaCosts;

        /**
         * 费用计算方式  0 按商品  1 按订单  （ 费用类型 2 外卖配送费不考虑该字段）
         */
        private Integer storeCalculationType;

    }
}

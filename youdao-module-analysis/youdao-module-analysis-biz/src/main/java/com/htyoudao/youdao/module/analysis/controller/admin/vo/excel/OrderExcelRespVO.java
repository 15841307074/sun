package com.htyoudao.youdao.module.analysis.controller.admin.vo.excel;

import cn.hutool.core.collection.CollectionUtil;
import com.alibaba.excel.annotation.ExcelIgnore;
import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.converters.doubleconverter.DoubleStringConverter;
import com.alibaba.excel.converters.integer.IntegerStringConverter;
import com.alibaba.excel.converters.longconverter.LongStringConverter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.htyoudao.youdao.module.analysis.util.excelconverter.*;
import lombok.Data;
import org.springframework.data.annotation.Transient;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

/**
 * @author dht
 * 订单下载
 */
@Data
public class OrderExcelRespVO implements Serializable {

    @ExcelProperty(value = "下单时间",converter = DateTimeConverter.class)
    private Date createTime;

    @ExcelProperty(value = "门店名称")
    private String storeName;


    @ExcelProperty(value = "门店编号",converter = LongStringConverter.class)
    private Long storeId;

    @ExcelProperty(value = "城市")
    private String city;

    @ExcelProperty(value = "上级组织")
    private String org;

    /**
     * 支付方式 0 现金  是否不付款下单
     */
    @ExcelProperty(value = "是否不付款下单")
    private String paymentCode;


    @ExcelProperty(value = "订单单号")
    private String orderSn;

    /**
     * 订单状态：0-已取消；10-未付款订单；20-已付款；30-待取餐；40-代配送 50-已配送 60-已完成; [ES]
     */
    @ExcelProperty(value = "订单状态",converter = OrderStatusConverter.class)
    private Integer orderState;

    /**
     * 订单完成时间 [ES]
     */
    @ExcelProperty(value = "完成时间",converter = LocalDateConverter.class)
    private LocalDateTime finishTime;


    /**
     * 下单方式 0点餐机 1微信小程序 2支付宝
     */
    @ExcelProperty(value = "下单方式",converter = OrderFromConverter.class)
    private Integer orderFrom;

    /**
     * 订单类型：0 堂食 1 打包 2 外卖 3 预订单
     */
    @ExcelProperty(value = "订单类型",converter = OrderTypeConverter.class)
    private Integer orderType;


    @ExcelProperty(value = "支付方式")
    private String paymentName;

    /**
     * 临时存储是否是会员下单 0否 1是
     */
    @ExcelProperty(value = "是否会员",converter = SettlementConverter.class)
    private Integer isSettlement;

    /**
     * 老客0 新客1
     */
    @ExcelProperty(value = "新老顾客",converter = ExpressConverter.class)
    private Long expressId;


    /**
     * 商品数
     */
    @Transient
    @JsonIgnore
    @ExcelProperty(value = "商品数",converter = IntegerStringConverter.class)
    private Integer productNum;

    /**
     * 商品信息
     */
    @Transient
    @JsonIgnore
    @ExcelProperty(value = "商品信息")
    private String productName;

    /**
     * 商品信息
     */
    @ExcelIgnore
    private List<Product> product;


    /**
     * 订单原价 [ES]
     */
    @ExcelProperty(value = "订单原价",converter = DoubleStringConverter.class)
    private Double orderAmount;

    /**
     * 三方支付金额 [ES]
     */
    @ExcelProperty(value = "顾客实付",converter = DoubleStringConverter.class)
    private Double payAmount;

    /**
     * 优惠券面额 [ES]
     */
    @ExcelProperty(value = "优惠金额",converter = DoubleStringConverter.class)
    private Double voucherPrice;

    /**
     * 打包费 [ES]
     */
    @ExcelProperty(value = "打包费",converter = DoubleStringConverter.class)
    private Double packingCharge;

    /**
     * 物流费用 [ES]
     */
    @ExcelProperty(value = "配送费",converter = DoubleStringConverter.class)
    private Double expressFee;

    @Data
    public static class Product {
        @ExcelIgnore
        private Long commodityId;
        @ExcelIgnore
        private String goodsName;
        @ExcelIgnore
        private Integer goodsNum;
    }
}

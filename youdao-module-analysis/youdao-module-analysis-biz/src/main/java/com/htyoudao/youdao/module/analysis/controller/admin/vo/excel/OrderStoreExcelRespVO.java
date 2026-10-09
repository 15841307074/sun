package com.htyoudao.youdao.module.analysis.controller.admin.vo.excel;

import com.alibaba.excel.annotation.ExcelIgnore;
import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.converters.doubleconverter.DoubleStringConverter;
import com.alibaba.excel.converters.integer.IntegerStringConverter;
import com.alibaba.excel.converters.longconverter.LongStringConverter;
import com.alibaba.fastjson2.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.htyoudao.youdao.module.analysis.util.excelconverter.*;
import lombok.Data;
import org.springframework.data.annotation.Transient;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

/**
 * @author dht
 * 订单下载
 * 以createTime为准
 */
@Data
public class OrderStoreExcelRespVO implements Serializable {

    @ExcelProperty(value = "门店名称")
    private String storeName;


    @ExcelProperty(value = "订单号")
    private String orderSn;

    /**
     * 订单类型：0 堂食 1 打包 2 外卖 3 预订单
     */
    @ExcelProperty(value = "订单类型",converter = OrderTypeConverter.class)
    private Integer orderType;

    @ExcelProperty(value = "支付方式")
    private String paymentName;
    /**
     * 下单方式 0点餐机 1微信小程序 2支付宝
     */
    @ExcelProperty(value = "订单渠道",converter = OrderFromConverter.class)
    private Integer orderFrom;

    @ExcelProperty(value = "下单时间",converter = DateTimeConverter.class)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @JSONField(format="yyyy-MM-dd HH:mm:ss")
    private Date createTime;
    /**
     * 订单状态：0-已取消；10-未付款订单；20-已付款；30-待取餐；40-代配送 50-已配送 60-已完成; [ES]
     */
    @ExcelProperty(value = "订单状态",converter = OrderStatusConverter.class)
    private Integer orderState;




    @ExcelProperty(value = "商品名称")
    private String goodsName;

    /**
     * 商品单价
     */
    @ExcelProperty(value = "商品原价(元)")
    private Double goodsAmount;

    @ExcelProperty(value = "购买数量")
    private Integer goodsNum;


    @ExcelProperty(value = "商品小计(元)")
    private Double goodsSubtotal;
    /**
     * 优惠活动价格
     */
    @ExcelProperty(value = "活动优惠(元)")
    private Double promotionDiscountAmount;

//    @ExcelProperty(value = "优惠总金额(元)")
//    private Double discountAmount;


    @ExcelProperty(value = "商品合计(元)")
    private Double commodityPrice;

//    @ExcelProperty(value = "营销活动")
//    private String activityName;

//
//    /**
//     * 优惠卷名称
//     */
//    @ExcelProperty(value = "优惠卷")
//    private String couponName;
//
//
//    /**
//     * 优惠优惠卷金额
//     */
//    @ExcelProperty(value = "优惠卷抵扣(元)")
//    private Double activityDiscountAmount;


















//    /**
//     * 商品信息
//     */
//    @ExcelIgnore
//    private List<Product> product;



//    /**
//     * 临时存储是否是会员下单 0否 1是
//     */
//    @ExcelProperty(value = "是否会员",converter = SettlementConverter.class)
//    private Integer isSettlement;
//
//    /**
//     * 老客0 新客1
//     */
//    @ExcelProperty(value = "新老顾客",converter = ExpressConverter.class)
//    private Long expressId;
//
//
//    /**
//     * 商品数
//     */
//    @Transient
//    @JsonIgnore
//    @ExcelProperty(value = "商品数",converter = IntegerStringConverter.class)
//    private Integer productNum;
//
//    /**
//     * 商品信息
//     */
//    @Transient
//    @JsonIgnore
//    @ExcelProperty(value = "商品信息")
//    private String productName;
//
//
//
//
//
//    @Data
//    public static class Product {
//        @ExcelIgnore
//        private Long commodityId;
//        @ExcelIgnore
//        private String goodsName;
//        @ExcelIgnore
//        private Double goodsAmount;
//        @ExcelIgnore
//        private Integer goodsNum;
//
//
//    }
}

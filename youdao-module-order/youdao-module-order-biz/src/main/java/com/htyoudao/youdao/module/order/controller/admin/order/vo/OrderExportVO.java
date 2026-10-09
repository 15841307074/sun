package com.htyoudao.youdao.module.order.controller.admin.order.vo;

import com.alibaba.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class OrderExportVO implements Serializable {

    @Serial
    private static final long serialVersionUID = -5510112731030668365L;

    @ExcelProperty("订单号")
    private String orderSn;

    @ExcelProperty("下单手机")
    private String takeAwayTel;

    @ExcelProperty("门店名称")
    private String storeName;

    @ExcelProperty("订单类型")
    private String orderType;

    @ExcelProperty("订单状态")
    private String orderState;
    @ExcelProperty("订单渠道")
    private String orderFrom;

    @ExcelProperty("支付方式")
    private String paymentCode;

    @ExcelProperty("下单时间")
    private String createTime;

    @ExcelProperty("取餐号")
    private String pickUpNum;

    @ExcelProperty("优惠金额")
    private BigDecimal activityDiscountAmount;

    @ExcelProperty("实付金额")
    private BigDecimal payAmount;
}

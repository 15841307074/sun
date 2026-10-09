// ChannelElemeOrder.java
package com.htyoudao.youdao.module.commodity.dal.dto.otherorder;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import jakarta.validation.constraints.NotBlank;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = false) // 设置 chain = false，避免用户导入有问题
@ExcelIgnoreUnannotated
public class ChannelElemeOrder {

    @ExcelProperty(index = 0, value = "日期")
    private String date;
    @ExcelProperty(index = 1, value = "门店名称")
    @NotBlank(message = "门店名称不能为空")
    private String storeName;
    @ExcelProperty(index = 2, value = "门店编号")
    @NotBlank(message = "门店编号不能为空")
    private String storeCode;
    @ExcelProperty(index = 3, value = "门店所在城市")
    private String city;
    @ExcelProperty(index = 4, value = "订单单号")
    @NotBlank(message = "订单单号不能为空")
    private String orderNumber;
    @ExcelProperty(index = 5, value = "下单时间")
    private String orderTime;
    @ExcelProperty(index = 6, value = "是否抖音渠道订单")
    private String isDouyinOrder;
    @ExcelProperty(index = 7, value = "接单时间")
    private String acceptTime;
    @ExcelProperty(index = 8, value = "出餐时长")
    private String prepareDuration;
    @ExcelProperty(index = 9, value = "完成时间")
    private String completeTime;
    @ExcelProperty(index = 10, value = "订单状态")
    private String orderStatus;
    @ExcelProperty(index = 11, value = "是否预订单")
    private String isPreOrder;
    @ExcelProperty(index = 12, value = "是否企业订单")
    private String isEnterpriseOrder;
    @ExcelProperty(index = 13, value = "配送方式")
    private String deliveryMethod;
    @ExcelProperty(index = 14, value = "支付方式")
    private String paymentMethod;
    @ExcelProperty(index = 15, value = "是否品牌会员")
    private String isBrandMember;
    @ExcelProperty(index = 16, value = "新老顾客")
    private String customerType;
    @ExcelProperty(index = 17, value = "无效原因")
    private String invalidReason;
    @ExcelProperty(index = 18, value = "商品数")
    private Integer productCount;
    @ExcelProperty(index = 19, value = "商品信息")
    @NotBlank(message = "商品信息不能为空")
    private String productInfo;
    @ExcelProperty(index = 20, value = "活动信息")
    private String activityInfo;
    @ExcelProperty(index = 21, value = "账单日期")
    private String billDate;
    @ExcelProperty(index = 22, value = "订单类型")
    private String orderType;
    @ExcelProperty(index = 23, value = "订单原价")
    private BigDecimal originalPrice;
    @ExcelProperty(index = 24, value = "顾客实付")
    private BigDecimal customerPaid;
    @ExcelProperty(index = 25, value = "菜品原价")
    private BigDecimal dishOriginalPrice;
    @ExcelProperty(index = 26, value = "餐盒费")
    private BigDecimal packageFee;
    @ExcelProperty(index = 27, value = "配送费")
    private BigDecimal deliveryFee;
    @ExcelProperty(index = 28, value = "活动总补贴")
    private BigDecimal totalSubsidy;
    @ExcelProperty(index = 29, value = "商家成本")
    private BigDecimal merchantCost;
    @ExcelProperty(index = 30, value = "饿了么补贴")
    private BigDecimal elemeSubsidy;
    @ExcelProperty(index = 31, value = "失败原因")
    private String errorMessage;
}
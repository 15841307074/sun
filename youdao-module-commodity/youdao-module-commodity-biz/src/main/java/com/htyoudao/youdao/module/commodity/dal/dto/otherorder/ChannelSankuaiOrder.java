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
public class ChannelSankuaiOrder {

    @ExcelProperty(index = 0, value = "日期")
    private String date;
    @ExcelProperty(index = 1, value = "订单编号")
    @NotBlank(message = "订单编号不能为空")
    private String orderNumber;
    @ExcelProperty(index = 2, value = "门店所在城市")
    private String city;
    @ExcelProperty(index = 3, value = "门店名称")
    @NotBlank(message = "门店名称不能为空")
    private String storeName;
    @ExcelProperty(index = 4, value = "门店id")
    @NotBlank(message = "门店id不能为空")
    private String storeCode;
    @ExcelProperty(index = 5, value = "订单序号")
    private String orderSequence;
    @ExcelProperty(index = 6, value = "下单时间")
    private String orderTime;
    @ExcelProperty(index = 7, value = "完成时间")
    private String completeTime;
    @ExcelProperty(index = 8, value = "配送时长")
    private String deliveryDuration;
    @ExcelProperty(index = 9, value = "订单状态")
    private String orderStatus;
    @ExcelProperty(index = 10, value = "是否预订单")
    private String isPreOrder;
    @ExcelProperty(index = 11, value = "是否到店自取")
    private String isPickup;
    @ExcelProperty(index = 12, value = "商品信息")
    @NotBlank(message = "商品信息不能为空")
    private String productInfo;
    @ExcelProperty(index = 13, value = "活动信息")
    private String activityInfo;
    @ExcelProperty(index = 14, value = "配送类型")
    private String deliveryType;
    @ExcelProperty(index = 15, value = "是否部分退款")
    private String isPartialRefund;
    @ExcelProperty(index = 16, value = "是否全额退款")
    private String isFullRefund;
    @ExcelProperty(index = 17, value = "配送距离")
    private Integer deliveryDistance;
    @ExcelProperty(index = 18, value = "跑腿配送费")
    private BigDecimal deliveryFee;
    @ExcelProperty(index = 19, value = "跑腿加小费")
    private BigDecimal deliveryTip;
    @ExcelProperty(index = 20, value = "订单实付")
    private BigDecimal customerPaid;
    @ExcelProperty(index = 21, value = "商品原价")
    private BigDecimal dishOriginalPrice;
    @ExcelProperty(index = 22, value = "配送费")
    private BigDecimal platformDeliveryFee;
    @ExcelProperty(index = 23, value = "包装费")
    private BigDecimal packageFee;
    @ExcelProperty(index = 24, value = "活动补贴(平台+商家)")
    private BigDecimal activitySubsidy;
    @ExcelProperty(index = 25, value = "商家活动支出")
    private BigDecimal merchantCost;
    @ExcelProperty(index = 26, value = "是否商责取消")
    private String isMerchantCancel;
    @ExcelProperty(index = 27, value = "商责取消原因")
    private String cancelReason;
    @ExcelProperty(index = 28, value = "是否配送延迟")
    private String isDeliveryDelay;
    @ExcelProperty(index = 29, value = "是否有效配送信息回传")
    private String isValidDeliveryInfo;
    @ExcelProperty(index = 30, value = "配送信息回传无效原因")
    private String invalidDeliveryReason;
    @ExcelProperty(index = 31, value = "失败原因")
    private String errorMessage;
}
package com.htyoudao.youdao.module.commodity.controller.admin.inventory.vo;

import com.alibaba.excel.annotation.ExcelProperty;
import com.htyoudao.youdao.module.commodity.enums.ChannelType;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CheckChannelOrderDTO {

    @ExcelProperty(index = 1, value = "订单编号")
    @NotBlank(message = "订单编号不能为空")
    private String orderNumber;

    @ExcelProperty(index = 3, value = "门店名称")
    @NotBlank(message = "门店名称不能为空")
    private String storeName;

    @ExcelProperty(index = 4, value = "门店id")
    @NotBlank(message = "门店id不能为空")
    private String storeCode;

    private Long storeId;

    @ExcelProperty(index = 9, value = "订单状态")
    private String orderStatus;

    @ExcelProperty(index = 12, value = "商品信息")
    @NotBlank(message = "商品信息不能为空")
    private String productInfo;

}
package com.htyoudao.youdao.module.analysis.controller.admin.vo.res.activity;

import com.alibaba.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "满赠活动门店数据导出 VO")
public class ActivityMzStoreExcelVO {

    @ExcelProperty(value = "门店名称")
    @Schema(description = "门店名称")
    private String storeName;

    @ExcelProperty(value = "付款用户数")
    @Schema(description = "付款用户数")
    private Long customerCount;

    @ExcelProperty(value = "订单数")
    @Schema(description = "订单数")
    private Long orderNumber;

    @ExcelProperty(value = "支付总金额")
    @Schema(description = "支付总金额")
    private Double payAmount;

    @ExcelProperty(value = "购买商品件数")
    @Schema(description = "购买商品件数")
    private Long commodityCount;

    @ExcelProperty(value = "赠送商品数量")
    @Schema(description = "赠送商品数量")
    private Long giftCommodityCount;

    @ExcelProperty(value = "活动优惠")
    @Schema(description = "活动优惠")
    private Double offerAmount;
}

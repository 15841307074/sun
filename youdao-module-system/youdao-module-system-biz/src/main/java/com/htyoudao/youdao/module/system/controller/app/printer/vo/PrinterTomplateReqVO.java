package com.htyoudao.youdao.module.system.controller.app.printer.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class PrinterTomplateReqVO {
    @Schema(description = "门店id", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long storeId;
    @Schema(description = "模板类型 1 商家 2 客户  3 后厨 4 配送", requiredMode = Schema.RequiredMode.REQUIRED, example = "0090")
    private Integer  printerType;
    @Schema(description = "打印内容设置")
    private PrintConfigVO printConfig;

//    @Schema(description = "打印内容设置")
//    private PrintAllInfoVo printAllInfoVo;


    @Schema(description = "顾客信息")
    private InformationVO customerInformation = new InformationVO();
    @Schema(description = "备注信息")
    private InformationVO remarksInformation= new InformationVO();
    @Schema(description = "商品名称")
    private ProductInfoVO productInformation= new ProductInfoVO();
    @Schema(description = "商品详细")
    private ProductInfoVO productDetailedInformation= new ProductInfoVO();
    @Schema(description = "加购信息")
    private ProductInfoVO additionalPurchaseInformation= new ProductInfoVO();
    @Schema(description = "总计信息")
    private InformationVO totalInformation= new InformationVO();
    @Schema(description = "其他信息")
    private InformationVO otherInformation= new InformationVO();
    @Schema(description = "订单信息。")
    private InformationVO orderInformation = new InformationVO();
    @Schema(description = "配送信息，仅在配送角色中存在。")
    private InformationVO deliveryInformation = new InformationVO();
    @Schema(description = "商家备注，存在商家备注时为 true，否则为 false。")
    private boolean merchantRemarks = false;
    @Schema(description = "二维码展示")
    private boolean merchantQrCode = false;
    @Schema(description = "取餐信息")
    private InformationVO pickUpInformation =  new InformationVO();

}

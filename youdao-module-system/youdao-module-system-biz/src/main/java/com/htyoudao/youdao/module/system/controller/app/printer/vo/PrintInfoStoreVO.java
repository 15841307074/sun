package com.htyoudao.youdao.module.system.controller.app.printer.vo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 该类表示每个角色（如店铺、会员等）具体的打印信息，包含各种信息类型和商家备注。
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class PrintInfoStoreVO {

    @Schema(description = "取餐信息")
    private InformationVO pickUpInformation =  new InformationVO();
    @Schema(description = "顾客信息")
    private InformationVO customerInformation = new InformationVO();
    @Schema(description = "备注信息")
    private InformationVO remarksInformation= new InformationVO();
    @Schema(description = "商品信息")
    private ProductInfoVO productInformation= new ProductInfoVO();
    @Schema(description = "商品详细")
    private ProductInfoVO productDetailedInformation= new ProductInfoVO();
    @Schema(description = "加购信息")
    private ProductInfoVO additionalPurchaseInformation= new ProductInfoVO();
    @Schema(description = "总计信息")
    private InformationVO totalInformation= new InformationVO();
    @Schema(description = "其他信息")
    private ProductInfoVO otherInformation= new ProductInfoVO();
    @Schema(description = "订单信息")
    private InformationVO orderInformation = new InformationVO();
    @Schema(description = "商家备注，存在商家备注时为 true，否则为 false")
    private boolean merchantRemarks = false;
    @Schema(description = "商家二维码")
    private boolean merchantQrCode = false;
    @Schema(description = "配送信息，仅在配送角色中存在。")
    private InformationVO deliveryInformation = new InformationVO();

}
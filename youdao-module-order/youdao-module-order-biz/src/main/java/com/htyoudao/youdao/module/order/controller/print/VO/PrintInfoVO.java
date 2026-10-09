package com.htyoudao.youdao.module.order.controller.print.VO;

import lombok.Data;

/**
 * 该类表示每个角色（如店铺、会员等）具体的打印信息，包含各种信息类型和商家备注。
 */
@Data
public class PrintInfoVO {
    /**
     * 取餐信息。
     */
    private InformationVO pickUpInformation;
    /**
     * 顾客信息。
     */
    private InformationVO customerInformation;
    /**
     * 备注信息。
     */
    private InformationVO remarksInformation;
    /**
     * 商品信息。
     */
    private ProductInfoVO productInformation;
    /**
     * 加购信息。
     */
    private ProductInfoVO additionalPurchaseInformation;
    /**
     * 总计信息。
     */
    private InformationVO totalInformation;
    /**
     * 其他信息。
     */
    private InformationVO otherInformation;
    /**
     * 订单信息。
     */
    private InformationVO orderInformation;
    /**
     * 配送信息，仅在配送角色中存在。
     */
    private InformationVO deliveryInformation;
    /**
     * 商家备注，存在商家备注时为 true，否则为 false。
     */
    private boolean merchantRemarks = false;
    /**
     * 商品详细。
     */
    private InformationVO productDetailedInformation;

}
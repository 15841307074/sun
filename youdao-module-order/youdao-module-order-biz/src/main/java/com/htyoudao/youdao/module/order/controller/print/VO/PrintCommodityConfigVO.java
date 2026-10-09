package com.htyoudao.youdao.module.order.controller.print.VO;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

/**
 * 该类代表整个打印配置，包含了不同角色（如店铺、会员、厨房、配送）的打印信息。
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class PrintCommodityConfigVO {
    private ProductInfoVO productInformation;
    private ProductInfoVO productDetailedInformation;
    private ProductInfoVO additionalPurchaseInformation;

    public ProductInfoVO getProductInformation() {
        return productInformation != null ? productInformation : new ProductInfoVO();
    }

    public ProductInfoVO getProductDetailedInformation() {
        return productDetailedInformation != null ? productDetailedInformation : new ProductInfoVO();
    }

    public ProductInfoVO getAdditionalPurchaseInformation() {
        return additionalPurchaseInformation != null ? additionalPurchaseInformation : new ProductInfoVO();
    }
}
package com.htyoudao.youdao.module.commodity.controller.admin.spus.VO;


import com.htyoudao.youdao.module.commodity.dal.dataobject.CommoditySkus;
import com.htyoudao.youdao.module.commodity.dal.dataobject.CommodityStoreSku;
import com.htyoudao.youdao.module.commodity.dal.dataobject.CommodityTemplateSkus;
import java.util.Objects;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Data
public class PriceResultDTO {

    private BigDecimal minIllustratePrices;
    private BigDecimal maxIllustratePrices;
    private BigDecimal minStrikeThroughPrice;
    private BigDecimal maxStrikeThroughPrice;


    public PriceResultDTO(BigDecimal minIllustratePrices, BigDecimal maxIllustratePrices,
                          BigDecimal minStrikeThroughPrice, BigDecimal maxStrikeThroughPrice) {
        this.minIllustratePrices = minIllustratePrices;
        this.maxIllustratePrices = maxIllustratePrices;
        this.minStrikeThroughPrice = minStrikeThroughPrice;
        this.maxStrikeThroughPrice = maxStrikeThroughPrice;
    }
    public BigDecimal getMinIllustratePrices() {
        return minIllustratePrices;
    }
    public BigDecimal getMaxIllustratePrices() {
        return maxIllustratePrices;
    }
    public BigDecimal getMinStrikeThroughPrice() {
        return minStrikeThroughPrice;
    }
    public BigDecimal getMaxStrikeThroughPrice() {
        return maxStrikeThroughPrice;
    }


    public static PriceResultDTO findMinMaxPrices(List<CommoditySkus> commoditySkuses) {
        if (commoditySkuses == null || commoditySkuses.isEmpty()) {
            return new PriceResultDTO(null, null, null, null);
        }
        // 找到最低和最高的 illustratePrices
        Optional<BigDecimal> minIllustratePrices = commoditySkuses.stream()
                .map(CommoditySkus::getIllustratePrices)
                .filter(price -> price != null)
                .min(Comparator.naturalOrder());
        Optional<BigDecimal> maxIllustratePrices = commoditySkuses.stream()
                .map(CommoditySkus::getIllustratePrices)
                .filter(price -> price != null)
                .max(Comparator.naturalOrder());
        // 找到最低和最高的 strikeThroughPrice
        Optional<BigDecimal> minStrikeThroughPrice = commoditySkuses.stream()
                .map(CommoditySkus::getStrikeThroughPrice)
                .filter(price -> price != null)
                .min(Comparator.naturalOrder());
        Optional<BigDecimal> maxStrikeThroughPrice = commoditySkuses.stream()
                .map(CommoditySkus::getStrikeThroughPrice)
                .filter(price -> price != null)
                .max(Comparator.naturalOrder());
        return new PriceResultDTO(
                minIllustratePrices.orElse(null),
                maxIllustratePrices.orElse(null),
                minStrikeThroughPrice.orElse(null),
                maxStrikeThroughPrice.orElse(null)
        );
    }
    public static PriceResultDTO findMinMaxPricesTemplate(List<CommodityTemplateSkus> commoditySkuses) {
        if (commoditySkuses == null || commoditySkuses.isEmpty()) {
            return new PriceResultDTO(null, null, null, null);
        }
        // 找到最低和最高的 illustratePrices
        Optional<BigDecimal> minIllustratePrices = commoditySkuses.stream()
                .map(CommodityTemplateSkus::getIllustratePrices)
                .filter(price -> price != null)
                .min(Comparator.naturalOrder());
        Optional<BigDecimal> maxIllustratePrices = commoditySkuses.stream()
                .map(CommodityTemplateSkus::getIllustratePrices)
                .filter(price -> price != null)
                .max(Comparator.naturalOrder());
        // 找到最低和最高的 strikeThroughPrice
        Optional<BigDecimal> minStrikeThroughPrice = commoditySkuses.stream()
                .map(CommodityTemplateSkus::getStrikeThroughPrice)
                .filter(price -> price != null)
                .min(Comparator.naturalOrder());
        Optional<BigDecimal> maxStrikeThroughPrice = commoditySkuses.stream()
                .map(CommodityTemplateSkus::getStrikeThroughPrice)
                .filter(price -> price != null)
                .max(Comparator.naturalOrder());
        return new PriceResultDTO(
                minIllustratePrices.orElse(null),
                maxIllustratePrices.orElse(null),
                minStrikeThroughPrice.orElse(null),
                maxStrikeThroughPrice.orElse(null)
        );
    }
    public static PriceResultDTO findMinMaxPricesStore(List<CommodityStoreSku> commoditySkuses) {
        if (commoditySkuses == null || commoditySkuses.isEmpty()) {
            return new PriceResultDTO(null, null, null, null);
        }
        // 找到最低和最高的 illustratePrices
        Optional<BigDecimal> minIllustratePrices = commoditySkuses.stream()
                .map(CommodityStoreSku::getCommodityStoreSkuPrice)
                .filter(Objects::nonNull)
                .min(Comparator.naturalOrder());
        Optional<BigDecimal> maxIllustratePrices = commoditySkuses.stream()
                .map(CommodityStoreSku::getCommodityStoreSkuPrice)
                .filter(Objects::nonNull)
                .max(Comparator.naturalOrder());
        // 找到最低和最高的 strikeThroughPrice
        Optional<BigDecimal> minStrikeThroughPrice = commoditySkuses.stream()
                .map(CommodityStoreSku::getCommodityStoreSkuStrikePrice)
                .filter(Objects::nonNull)
                .min(Comparator.naturalOrder());
        Optional<BigDecimal> maxStrikeThroughPrice = commoditySkuses.stream()
                .map(CommodityStoreSku::getCommodityStoreSkuStrikePrice)
                .filter(Objects::nonNull)
                .max(Comparator.naturalOrder());
        return new PriceResultDTO(
                minIllustratePrices.orElse(null),
                maxIllustratePrices.orElse(null),
                minStrikeThroughPrice.orElse(null),
                maxStrikeThroughPrice.orElse(null)
        );
    }
}

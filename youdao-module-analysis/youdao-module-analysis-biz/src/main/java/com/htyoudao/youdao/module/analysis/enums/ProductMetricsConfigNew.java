package com.htyoudao.youdao.module.analysis.enums;


import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery.Builder;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import com.htyoudao.youdao.module.analysis.enums.MetricsConfig.AggregationType;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.util.CollectionUtils;

/**
 * 商品指标
 */
@Getter
@AllArgsConstructor
public enum ProductMetricsConfigNew {

    COMMODITY_ID("commodityId","商品ID", AggregationType.SIZE,"commodityId"),
    GOODS_NAME("goodsName","商品名", AggregationType.HIT,"goodsName"),
    CATEGORY_NAME("categoryName","分类名", AggregationType.HIT,"categoryName"),

    GOODS_IMAGE("goodsImage","商品图片", AggregationType.HIT,"goodsImage"),
    IS_SINGLE("isSingle","是否套餐", AggregationType.SIZE,"isSingle"),
    IS_PURCHASE("isPurchase","是否加购商品", AggregationType.HIT,"isPurchase"),

    SALES_AMOUNT("salesAmount","销售额", AggregationType.SUM,"goodsAmount"),
    PAY_AMOUNT("payAmount","优惠后销售额", AggregationType.SUM,"payAmount"),
    SALES_VOLUME("salesVolume","单独售卖销量", AggregationType.SUM,"goodsNum"),
    SINGLE_SALES_VOLUME("singleSalesVolume","套餐内单品销量", AggregationType.SUM,"goodsNum"),
    ALL_SALES_VOLUME("allSalesVolume","总销量", AggregationType.SUM,"goodsNum"),

    ORDER_COUNT("orderCount","带来订单数", AggregationType.CARDINALITY,"orderId"),
    CUSTOMER_COUNT("customerCount","下单人数", AggregationType.CARDINALITY,"memberId"),
    NEW_CUSTOMER_COUNT("newCustomerCount","新客人数", AggregationType.CARDINALITY,"memberId"),

    REPEAT_BUYERS("repurchaseUserCount", "复购人数", AggregationType.MIN_COUNT, "memberId"),
    ALL_ACTIVITY_DISCOUNT("allDiscountAmount","优惠金额" , AggregationType.SUM,"allDiscountAmount" ),
    ACTIVITY_DISCOUNT_AMOUNT("activityDiscountAmount", "优惠券优惠金额", AggregationType.SUM, "activityDiscountAmount"),
    PROMOTION_DISCOUNT_AMOUNT("promotionDiscountAmount", "活动优惠金额", AggregationType.SUM, "promotionDiscountAmount"),

    COUPON_USE_COUNT("couponUseCount","优惠券使用数量", AggregationType.CARDINALITY,"orderId"),

    STORE_NAMES("storeName", "门店名称", AggregationType.HIT, "storeName"),
    ;

    private final String code;
    private final String name;
    private final AggregationType aggType;
    private final String field;

        ;

    public static ProductMetricsConfigNew getEnumByCode(String code) {
        for (ProductMetricsConfigNew metricsConfig : ProductMetricsConfigNew.values()) {
            if (metricsConfig.getCode().equals(code)) {
                return metricsConfig;
            }
        }
        return null;
    }

    public Query addTermsCondition(Builder boolBuilder) {
        switch (this){
            case SALES_VOLUME -> addTermsCondition(boolBuilder, "isSon", List.of(0));
            case SINGLE_SALES_VOLUME -> addTermsCondition(boolBuilder, "isSon", List.of(1));
            case NEW_CUSTOMER_COUNT -> addTermsCondition(boolBuilder, "expressId", List.of(1));
        }
        return new Query.Builder().bool(boolBuilder.build()).build();
    }

    //添加 terms 查询条件到 BoolQuery
    public void addTermsCondition(Builder boolBuilder, String fieldName, List<?> values) {
        if (!CollectionUtils.isEmpty(values)) {
            boolBuilder.must(m -> m
                .terms(t -> t
                    .field(fieldName)
                    .terms(tv -> tv.value(values.stream()
                            .map(FieldValue::of)
                            .toList()
                        )
                    )
                ));
        }
    }

    // AggregationType 枚举
    public enum AggregationType {
        SIZE,
        SUM,
        VALUE_COUNT,
        CARDINALITY,
        MIN_COUNT,
        HIT,
    }

}

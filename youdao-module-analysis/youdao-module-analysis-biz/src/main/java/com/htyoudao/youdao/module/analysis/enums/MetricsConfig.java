package com.htyoudao.youdao.module.analysis.enums;

import static com.htyoudao.youdao.module.analysis.enums.MetricsConfig.AggregationType.*;
import static com.htyoudao.youdao.module.analysis.enums.MetricsConfig.QueryType.*;
import static com.htyoudao.youdao.module.analysis.enums.OrderStatusConstants.INVALID_ORDER_STATES;
import static com.htyoudao.youdao.module.analysis.enums.OrderStatusConstants.VALID_ORDER_STATES;

import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery.Builder;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;

import java.util.Collections;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.dubbo.common.bytecode.ClassGenerator.DC;
import org.springframework.util.CollectionUtils;

@Getter
@AllArgsConstructor
public enum MetricsConfig {


    // 总览指标
    ORDER_AMOUNT("orderAmount", "优惠前总额", SUM, "orderAmount", List.of(VALID_ORDER)),
    PAY_AMOUNT("payAmount", "实收额", SUM, "payAmount", List.of(VALID_ORDER, NOT_CASH)),
    DISCOUNT_AMOUNT("discountAmount", "优惠总额", SUM, "allDiscountAmount", List.of(VALID_ORDER)),
    AFTER_DISCOUNT_AMOUNT("afterDiscountAmount", "优惠后总额", SUM, "payAmount", List.of(VALID_ORDER)),
    CANTEEN_FOOD_ORDER_AMOUNT("canteenFoodOrderAmount", "堂食订单优惠前总额", SUM, "orderAmount", List.of(CANTEEN_FOOD, VALID_ORDER)),
    TAKEAWAY_ORDER_AMOUNT("takeawayOrderAmount", "外卖订单优惠前总额", SUM, "orderAmount", List.of(TAKEAWAY, VALID_ORDER)),
    PACK_ORDER_AMOUNT("packOrderAmount", "外带订单优惠前总额", SUM, "orderAmount", List.of(PACK, VALID_ORDER)),
    CASH_PAY_AMOUNT("cashPayAmount", "现金收款", SUM, "payAmount", List.of(CASH, VALID_ORDER)),
    VALID_ORDERS("validOrders", "有效订单", COUNT, "orderId", List.of(VALID_ORDER)),
    REPEAT_BUYERS("repeatBuyers", "复购人数", MIN_COUNT, "memberId", List.of(MINIAPP, VALID_ORDER)),
    CUSTOMER_COUNT("customerCount", "下单顾客数", CARDINALITY, "memberId", List.of(MINIAPP, VALID_ORDER)),
    NETR_AMT("netrAmt", "实际到账金额", SUM, "balanceAmount", List.of(VALID_ORDER)),

    //订单指标
    CASH_PAY_ORDERS("cashPayOrders", "不付款下单订单数", COUNT, "orderId", List.of(VALID_ORDER, CASH)),
    AVERAGE_PAYMENT("averagePayment", "客单价", AggregationType.AVG, "payAmount", List.of(VALID_ORDER)),
    INVALID_ORDERS("invalidOrders", "无效订单", AggregationType.COUNT, "orderId", List.of(INVALID_ORDER)),
    PACKING_CHARGE("packingCharge", "打包费", SUM, "packingCharge", List.of(VALID_ORDER)),
    MINIMUM_DELIVERY_FEE("minimumDeliveryFee", "配送费", SUM, "expressFee", List.of(VALID_ORDER)),
    CANTEEN_FOOD_ORDERS("canteenFoodOrders", "堂食订单数", COUNT, "orderId", List.of(CANTEEN_FOOD, VALID_ORDER)),
    TAKEAWAY_ORDERS("takeawayOrders", "外卖订单数", COUNT, "orderId", List.of(TAKEAWAY, VALID_ORDER)),
    PACK_ORDERS("packOrders", "外带订单数", COUNT, "orderId", List.of(PACK, VALID_ORDER)),
    ERRAND_STORE_SUBSIDY_AMOUNT("errandStoreSubsidyAmount", "校园配送补贴", SUM, "errandStoreSubsidyAmount", List.of(VALID_ORDER)),



    //门店指标
    DC_PAY_AMOUNT("dcPayAmount", "点餐机实收额", SUM, "payAmount", List.of(DC, VALID_ORDER, NOT_CASH)),
    MINI_PAY_AMOUNT("payAmount", "实收额", SUM, "payAmount", List.of(MINIAPP, VALID_ORDER, NOT_CASH)),
    MINI_VALID_ORDERS("validOrders", "有效订单", COUNT, "orderId", List.of(MINIAPP, VALID_ORDER)),
    MINI_AVERAGE_PAYMENT("averagePayment", "客单价", AggregationType.AVG, "payAmount", List.of(MINIAPP, VALID_ORDER)),
    MINI_CUSTOMER_COUNT("customerCount", "下单顾客数", CARDINALITY, "memberId", List.of(MINIAPP, VALID_ORDER)),
    MINI_REPEAT_BUYERS("repeatBuyers", "复购人数", MIN_COUNT, "memberId", List.of(MINIAPP, VALID_ORDER)),
    MINI_NEW_CUSTOMER_COUNT("newCustomerCount", "新客数量", CARDINALITY, "memberId", List.of(NEW_CUSTOMER, MINIAPP, VALID_ORDER)),
    STORE_NAME("storeName", "门店名称", AggregationType.HIT, "storeName", null),
    DC_VALID_ORDERS("dcValidOrders", "点餐机订单数", COUNT, "orderId", List.of(DC, VALID_ORDER)),
    DC_ORDER_AMOUNT("dcOrderAmount", "点餐机优惠前总额", SUM, "orderAmount", List.of(DC, VALID_ORDER)),


    // 顾客指标
    NEW_CUSTOMER_COUNT("newCustomerCount", "新客数量", AggregationType.CARDINALITY, "memberId", List.of(VALID_ORDER, NEW_CUSTOMER)),
    OLD_CUSTOMER_COUNT("oldCustomerCount", "老客数量", AggregationType.CARDINALITY, "memberId", List.of(VALID_ORDER, OLD_CUSTOMER)),
    MEMBER_CUSTOMER_COUNT("memberCustomerCount", "会员下单人数", AggregationType.CARDINALITY, "memberId", List.of(VALID_ORDER, IS_MEMBER)),
    NOT_MEMBER_CUSTOMER_COUNT("notMemberCustomerCount", "非会员下单人数", AggregationType.CARDINALITY, "memberId", List.of(VALID_ORDER, NOT_MEMBER)),

    // 报表下载
    //订单
    ORDER_DOWNLOAD("orderDownload", "订单报表下载", AggregationType.CARDINALITY, "memberId", List.of(VALID_ORDER)),


    //活动指标
    AVERAGE_PAYMENTS("averagePayment", "客单价", AVG, "payAmount", List.of(VALID_ORDER)),
    PAY_AMOUNTS("payAmount", "支付总金额", SUM, "payAmount", List.of(VALID_ORDER)),
    OFFER_AMOUNT("offerAmount", "优惠总额", SUM, "product.promotionDiscountAmount", List.of(VALID_ORDER)),
    COMMODITY_COUNT("commodityCount", "购买商品件数", SUM, "product.goodsNum", List.of(VALID_ORDER)),
    ORDER_NUMBER("orderNumber", "订单数", COUNT, "orderId", List.of(VALID_ORDER)),
    CUSTOMER_COUNTS("customerCount", "付款用户数", CARDINALITY, "memberId", List.of(VALID_ORDER)),
    CONVERSION_RATE("conversionRate", "转化率", null, null, List.of(VALID_ORDER)),

    POINT_NUMBER("pointNumber", "集点数", SUM, "pickUpNum", List.of(VALID_ORDER)),
    MEMBER_NUMBER("memberNumber", "参与人数", COUNT, "memberId", List.of(VALID_ORDER)),

    //活动下门店指标
    STORE_NAMES("storeName", "门店名称", AggregationType.HIT, "storeName", null),
    //活动下渠道指标
    CHANNEL_NAMES("channelName", "渠道名称", AggregationType.HIT, "evaluateState", null),
    //参与人员
// 会员相关指标
    MEMBER_NAME("memberName", "会员名称", HIT, "memberName", null),  // 会员名称：获取具体值
    MEMBER_MOBILE("memberMobile", "会员手机号", HIT, "takeAwayTel", null),  // 会员手机号：获取具体值
    PAY_TIME("payTime", "支付时间", HIT, "createTime", null),  // 支付时间：获取具体值
    COMMODITY_NAME("commodityName", "商品名称", HIT, "product.goodsName", null),  // 商品名称：嵌套字段获取具体值
    PAY_AMOUNT_SUM("payAmount", "支付金额", SUM, "payAmount", List.of(VALID_ORDER)),  // 支付金额：求和（假设关联有效订单）
    CHANNEL("channel", "渠道ID", HIT, "evaluateState", null),  // 渠道ID：获取具体值
    TOTAL_VIEWS("totalViews", "总浏览量", COUNT, "viewId", null),  // 总浏览量：统计所有浏览记录数
    TOTAL_VISITORS("totalVisitors", "总访客量", CARDINALITY, "visitorId", null),  // 总访客量：统计独立访客数


    ;

    private final String code;
    private final String name;
    private final AggregationType aggType;
    private final String field;

    private final List<QueryType> queryTypes;

    public static MetricsConfig getEnumByCode(String code) {
        for (MetricsConfig metricsConfig : MetricsConfig.values()) {
            if (metricsConfig.getCode().equals(code)) {
                return metricsConfig;
            }
        }
        return null;
    }


    @AllArgsConstructor
    @Getter
    public enum QueryType {

        //订单状态
        VALID_ORDER(VALID_ORDER_STATES, "orderState"),
        INVALID_ORDER(INVALID_ORDER_STATES, "orderState"),

        //订单类型
        CANTEEN_FOOD(List.of(OrderTypeEnum.CANTEEN_FOOD.getCode(), OrderTypeEnum.PACK.getCode(), OrderTypeEnum.ERRAND.getCode()), "orderType"),
        PACK(List.of(OrderTypeEnum.PACK.getCode()), "orderType"),
        TAKEAWAY(Collections.singletonList(OrderTypeEnum.TAKEAWAY.getCode()), "orderType"),

        //订单来源
        DC(List.of(OrderForm.DC.getForm()), "orderFrom"),
        MINIAPP(List.of(OrderForm.WECHAT.getForm(), OrderForm.ALIPAY.getForm()), "orderFrom"),

        //收款方式
        CASH(Collections.singletonList(PaymentMethodEnum.CASH.getCode()), "paymentCode"),
        NOT_CASH(List.of(PaymentMethodEnum.WECHAT_PAYMENT.getCode(),PaymentMethodEnum.ALIPAY_PAYMENT.getCode()), "paymentCode"),

        //新客
        NEW_CUSTOMER(Collections.singletonList(1), "expressId"),
        //老客
        OLD_CUSTOMER(Collections.singletonList(0), "expressId"),
        // 会员
        IS_MEMBER(Collections.singletonList(1), "isSettlement"),
        // 非会员
        NOT_MEMBER(Collections.singletonList(0), "isSettlement"),
        ;
        private final List<Integer> list;
        private final String fieldName;
        ;


    }


    // AggregationType 枚举
    public enum AggregationType {
        SUM,
        COUNT,
        AVG,
        MAX,
        MIN,
        CARDINALITY,
        HIT,
        MIN_COUNT//最小数量
    }

    public Query addTermsCondition(Builder boolBuilder) {
        if (!CollectionUtils.isEmpty(queryTypes)) {
            queryTypes.forEach(queryType ->
                    addTermsCondition(boolBuilder, queryType.fieldName, queryType.list)
            );
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

}
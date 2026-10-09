package com.htyoudao.youdao.module.analysis.enums;

import com.alibaba.excel.converters.Converter;
import com.htyoudao.youdao.framework.common.core.ArrayValuable;
import com.htyoudao.youdao.module.analysis.util.excelconverter.BigDecimalNumberConverter;
import com.htyoudao.youdao.module.analysis.util.excelconverter.LocalDateTimeConverter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author dht
 * 订单下载的导出字段 -- 目前没用
 */
public enum OrderExportField implements ArrayValuable<String> {
    // 基础信息
    STORE_NAME("门店名称", "storeName", "基础",String.class,null),
    STORE_CODE("门店编号", "storeId", "基础",Long.class,null),
    CITY("城市", "city", "基础",String.class,null),
    PARENT_ORG("上级组织", "org", "基础",String.class,null),
    ALLOW_UNPAID_ORDER("是否不付款下单", "paymentCode", "基础",Integer.class,null),

    // 履约信息
    ORDER_NO("订单单号", "orderSn", "履约",String.class,null),
    ORDER_TIME("下单时间", "payTime", "履约",LocalDateTime.class,LocalDateTimeConverter.class),
    ORDER_STATUS("订单状态", "orderState", "履约",Integer.class,null),
    COMPLETE_TIME("完成时间", "finishTime", "履约", LocalDateTime.class, LocalDateTimeConverter.class),
    ORDER_METHOD("下单方式", "orderFrom", "履约",Integer.class,null),
    ORDER_TYPE("订单类型", "orderType", "履约",Integer.class,null),
    PAYMENT_METHOD("支付方式", "paymentName", "履约",Integer.class,null),
    IS_MEMBER("是否会员", "isSettlement", "履约",Integer.class,null),
    CUSTOMER_TYPE("新老顾客", "expressId", "履约",Integer.class,null),
    PRODUCT_COUNT("商品数", "productNum", "履约",Integer.class,null),
    PRODUCT_INFO("商品信息", "productName", "履约",String.class,null),
    ACTIVITY_NAME("活动信息", "activityName", "履约",Integer.class,null),
    CHANNEL("订单渠道", "channelType", "履约",String.class,null),

    // 金额信息
    ORIGINAL_AMOUNT("订单原价", "orderAmount", "金额",BigDecimal.class,BigDecimalNumberConverter.class),
    ACTUAL_AMOUNT("顾客实付", "payAmount", "金额",BigDecimal.class,BigDecimalNumberConverter.class),
    DISCOUNT_AMOUNT("优惠金额", "allDiscountAmount", "金额",BigDecimal.class,BigDecimalNumberConverter.class),
    PACKING_FEE("打包费", "packingCharge", "金额",BigDecimal.class,BigDecimalNumberConverter.class),
    DELIVERY_FEE("配送费", "expressFee", "金额", BigDecimal.class,BigDecimalNumberConverter.class);

    // Excel表头显示名称
    private final String headerName;
    // 对应字段名
    private final String fieldName;
    // 字段分组
    private final String group;
    // 字段类型
    private final Class<?> fieldType;
    // 转换器
    private final Class<? extends Converter<?>> converter;

    OrderExportField(String headerName, String fieldName, String group,Class<?> fieldType,Class<? extends Converter<?>> converter) {
        this.headerName = headerName;
        this.fieldName = fieldName;
        this.group = group;
        this.fieldType = fieldType;
        this.converter = converter;
    }

    public static final String[] ARRAYS = Arrays.stream(values()).map(OrderExportField::getFieldName).toArray(String[]::new);

    /**
     * 根据分组获取字段
     * @param group group
     * @return List<OrderExportField>
     */
    public static List<OrderExportField> getByGroup(String group) {
        return Arrays.stream(values())
                .filter(field -> field.group.equals(group))
                .collect(Collectors.toList());
    }

    /**
     * 根据对应字段名获取字段
     *
     * @return List<OrderExportField>
     */
    public static List<OrderExportField> getFieldName(String fieldName) {
        return Arrays.stream(values())
                .filter(field -> field.fieldName.equals(fieldName))
                .collect(Collectors.toList());
    }
    // ---------- Getter 方法 ----------
    public String getHeaderName() {
        return headerName;
    }

    public String getFieldName() {
        return fieldName;
    }

    public String getGroup() {
        return group;
    }

    public Class<? extends Converter<?>> getConverter() {
        return converter;
    }

    @Override
    public String[] array() {
        return ARRAYS;
    }
}

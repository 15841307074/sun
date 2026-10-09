package com.htyoudao.youdao.module.promotion.enums.advertising.carousel;

import cn.hutool.core.util.ArrayUtil;
import com.alibaba.nacos.shaded.com.google.gson.Gson;
import com.htyoudao.youdao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.*;
import java.util.stream.Collectors;

@Getter
@AllArgsConstructor
public enum CouponTypeEnum implements ArrayValuable<Integer> {

    VOUCHER(0, "满减券"),

    DISCOUNT_COUPON(1, "折扣券"),

    EXCHANGE_COUPON(2, "兑换券"),

    GOODS_COUPON(5,"商品券");

    public static final Integer[] ARRAYS = Arrays.stream(values()).map(CouponTypeEnum::getValue).toArray(Integer[]::new);

    /**
     * 类型
     */
    private final Integer value;
    /**
     * 类型名
     */
    private final String name;


    public static final List<Integer> VALUES_LIST = Collections.unmodifiableList(
            Arrays.stream(values()).map(CouponTypeEnum::getValue).collect(Collectors.toList())
    );

    public static CouponTypeEnum valueOf(Integer value) {
        return ArrayUtil.firstMatch(couponTypeEnum -> couponTypeEnum.getValue().equals(value), values());
    }

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("code", value);
        map.put("description", name);
        return map;
    }

    public static String getEle(){
        List<Map<String, Object>> enumList = Arrays.stream(CouponTypeEnum.values())
                .map(CouponTypeEnum::toMap)
                .collect(Collectors.toList());
        return new Gson().toJson(enumList);
    }
}

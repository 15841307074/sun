package com.htyoudao.youdao.module.promotion.enums.advertising.carousel;

import cn.hutool.core.util.ArrayUtil;
import com.alibaba.nacos.shaded.com.google.gson.Gson;
import com.htyoudao.youdao.framework.common.core.ArrayValuable;
import com.htyoudao.youdao.framework.common.enums.WxAddressTypeEnum;
import com.htyoudao.youdao.module.system.enums.sms.SmsSceneEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Getter
@AllArgsConstructor
public enum CouponISCommonEnum implements ArrayValuable<Integer> {

    ALL(1, "全部可用"),

    PART(2, "部分门店可用");

    public static final Integer[] ARRAYS = Arrays.stream(values()).map(CouponISCommonEnum::getValue).toArray(Integer[]::new);

    /**
     * 类型
     */
    private final Integer value;
    /**
     * 类型名
     */
    private final String name;

    public static CouponISCommonEnum valueOf(Integer value) {
        return ArrayUtil.firstMatch(isCommonEnum -> isCommonEnum.getValue().equals(value), values());
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
        List<Map<String, Object>> enumList = Arrays.stream(CouponISCommonEnum.values())
                .map(CouponISCommonEnum::toMap)
                .collect(Collectors.toList());
        return new Gson().toJson(enumList);
    }
}

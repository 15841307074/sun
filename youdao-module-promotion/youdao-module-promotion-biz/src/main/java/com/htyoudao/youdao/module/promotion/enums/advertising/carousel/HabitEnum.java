package com.htyoudao.youdao.module.promotion.enums.advertising.carousel;

import cn.hutool.core.util.ArrayUtil;
import com.alibaba.nacos.shaded.com.google.gson.Gson;
import com.htyoudao.youdao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Getter
@AllArgsConstructor
public enum HabitEnum implements ArrayValuable<Integer> {

    ALL(0, "全部可用"),

    EAT_IN(1, "堂食可用"),

    EAT_OUT(2, "外卖可用");

    public static final Integer[] ARRAYS = Arrays.stream(values()).map(HabitEnum::getValue).toArray(Integer[]::new);

    /**
     * 类型
     */
    private final Integer value;
    /**
     * 类型名
     */
    private final String name;

    public static HabitEnum valueOf(Integer value) {
        return ArrayUtil.firstMatch(habitEnum -> habitEnum.getValue().equals(value), values());
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
        List<Map<String, Object>> enumList = Arrays.stream(HabitEnum.values())
                .map(HabitEnum::toMap)
                .collect(Collectors.toList());
        return new Gson().toJson(enumList);
    }
}

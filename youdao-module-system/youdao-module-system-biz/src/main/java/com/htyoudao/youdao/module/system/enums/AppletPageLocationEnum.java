package com.htyoudao.youdao.module.system.enums;

import cn.hutool.core.util.ArrayUtil;
import com.google.gson.Gson;
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
public enum AppletPageLocationEnum implements ArrayValuable<Integer> {

    INDEX(1, "首页"),

    RESERVATION(2, "点餐页"),

    POINT_SHOP(3,"积分商城")
    ;


    public static final Integer[] ARRAYS = Arrays.stream(values()).map(AppletPageLocationEnum::getValue).toArray(Integer[]::new);

    /**
     * 类型
     */
    private final Integer value;
    /**
     * 类型名
     */
    private final String name;

    public static AppletPageLocationEnum valueOf(Integer value) {
        return ArrayUtil.firstMatch(appletPageLocationEnum -> appletPageLocationEnum.getValue().equals(value), values());
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
        List<Map<String, Object>> enumList = Arrays.stream(AppletPageLocationEnum.values())
                .map(AppletPageLocationEnum::toMap)
                .collect(Collectors.toList());
        String json = new Gson().toJson(enumList);
        return json;
    }
}

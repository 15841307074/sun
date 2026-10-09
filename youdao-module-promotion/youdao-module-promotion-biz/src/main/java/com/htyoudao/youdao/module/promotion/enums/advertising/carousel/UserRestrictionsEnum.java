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
public enum UserRestrictionsEnum implements ArrayValuable<Integer> {

    ALL(0, "不限制"),

    NEW(1, "新注册用户"),

    OLD(2, "老用户"),

    REGRESSION(3,"回归用户");

    public static final Integer[] ARRAYS = Arrays.stream(values()).map(UserRestrictionsEnum::getValue).toArray(Integer[]::new);

    /**
     * 类型
     */
    private final Integer value;
    /**
     * 类型名
     */
    private final String name;

    public static UserRestrictionsEnum valueOf(Integer value) {
        return ArrayUtil.firstMatch(userRestrictionsEnum -> userRestrictionsEnum.getValue().equals(value), values());
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
        List<Map<String, Object>> enumList = Arrays.stream(UserRestrictionsEnum.values())
                .map(UserRestrictionsEnum::toMap)
                .collect(Collectors.toList());
        return new Gson().toJson(enumList);
    }

    public static String getValueByCode(Integer value){
        if (value == ALL.value) {
            return "";
        }
        UserRestrictionsEnum userRestrictionsEnum = UserRestrictionsEnum.valueOf(value);
        return userRestrictionsEnum.getName();
    }
}

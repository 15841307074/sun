package com.htyoudao.youdao.module.promotion.util;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

public class ConvertUtil {

    //将 List <Long > 转成 string
    public static String convertListToString(List<Long> longList) {
        return longList.stream().map(String::valueOf).collect(Collectors.joining(","));

    }

    //将 String 里面 long，long 转成 list<long>
    public static List<Long> convertStringToList(String longListAsString) {
        List<Long> collect = Arrays.stream(longListAsString.split(","))
            .map(Long::valueOf)
            .collect(Collectors.toList());
        collect.removeIf(Objects::isNull);
        // 使用逗号拆分字符串，并将每个部分转换为 Long 类型
        return collect;
    }

    // 将 List<Integer> 转成 String
    public static String convertInListToString(List<Integer> integerList) {
        return integerList.stream()
            .map(String::valueOf)
            .collect(Collectors.joining(","));
    }

    // 将 String 里面 integer,integer 转成 List<Integer>
    public static List<Integer> convertStringToInList(String integerListAsString) {
        return Arrays.stream(integerListAsString.split(","))
            .filter(str -> !str.isEmpty())
            .map(Integer::valueOf)
            .collect(Collectors.toList());
    }

    public static List<Long> convertStringToListNew(String longListAsString) {

        // 使用逗号拆分字符串，并将每个部分转换为 Long 类型
        return Arrays.stream(longListAsString.split(","))
            .map(Long::valueOf)
            .collect(Collectors.toList());
    }

    public static String convertListToStringS(List<String> stringList) {
        // 使用逗号连接 List<String> 中的元素
        return String.join(",", stringList);
    }

    public static List<String> convertStringToListS(String stringListAsString) {
        if (StringUtils.isBlank(stringListAsString)) {
            return new ArrayList<>();
        }
        // 使用逗号拆分字符串
        return Arrays.asList(stringListAsString.split(","));
    }

    private static final ObjectMapper DEFAULT_OBJECT_MAPPER = new ObjectMapper();


    public static <T> List<T> deserialize(String json) throws JsonProcessingException {
        if (ObjectUtils.isEmpty(json)) {
            return Collections.emptyList();
        }
        return DEFAULT_OBJECT_MAPPER.readValue(json, new com.fasterxml.jackson.core.type.TypeReference<List<T>>() {});
    }


}

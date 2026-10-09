package com.htyoudao.youdao.module.promotion.util;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class ImageValueParseUtil {

    private ImageValueParseUtil() {
    }

    public static List<String> parseImageValues(String rawValue) {
        if (rawValue == null || rawValue.isBlank()) {
            return List.of();
        }
        String trimmedValue = rawValue.trim();
        if (trimmedValue.startsWith("{")) {
            try {
                JSONObject jsonObject = JSON.parseObject(trimmedValue);
                List<String> imageValues = new ArrayList<>();
                collectStringValues(jsonObject, imageValues);
                return imageValues;
            } catch (Exception ignored) {
                // Compatible with historical comma-separated data.
            }
        }
        return Arrays.stream(trimmedValue.split(","))
                .map(String::trim)
                .filter(item -> !item.isEmpty())
                .toList();
    }

    private static void collectStringValues(Object value, List<String> imageValues) {
        if (value == null) {
            return;
        }
        if (value instanceof String stringValue) {
            if (!stringValue.isBlank()) {
                imageValues.add(stringValue.trim());
            }
            return;
        }
        if (value instanceof JSONObject jsonObject) {
            for (Object nestedValue : jsonObject.values()) {
                collectStringValues(nestedValue, imageValues);
            }
            return;
        }
        if (value instanceof JSONArray jsonArray) {
            for (Object nestedValue : jsonArray) {
                collectStringValues(nestedValue, imageValues);
            }
        }
    }
}

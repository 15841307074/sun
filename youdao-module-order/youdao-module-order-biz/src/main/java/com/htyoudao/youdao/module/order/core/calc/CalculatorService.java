package com.htyoudao.youdao.module.order.core.calc;

import org.springframework.util.ObjectUtils;

import java.util.HashMap;
import java.util.Map;

public class CalculatorService {

    protected Map<String, Integer> buildPeakHourMap(String peakHours, String mealTime) {

        // 默认值
        Map<String, Integer> defaultMap = new HashMap<>();
        defaultMap.put("00:00-23:59", 5);

        // 任一为空，直接返回默认值
        if (peakHours == null || peakHours.trim().isEmpty()
                || mealTime == null || mealTime.trim().isEmpty()) {
            return defaultMap;
        }

        String[] peakArr = peakHours.split(",");
        String[] mealArr = mealTime.split(",");

        // 长度不一致也返回默认值（避免数据错位）
        if (peakArr.length != mealArr.length) {
            return defaultMap;
        }

        Map<String, Integer> map = new HashMap<>();
        for (int i = 0; i < peakArr.length; i++) {
            if(ObjectUtils.isEmpty(peakArr[i])){
                continue;
            }
            map.put(peakArr[i], Integer.parseInt(mealArr[i]));
        }

        return map;
    }
}

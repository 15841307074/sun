package com.htyoudao.youdao.module.analysis.util;


import java.math.BigDecimal;
import java.math.RoundingMode;

public class PercentUtil {

    /**
     * 返回百分比数值（例如 14.8588 表示 14.8588%）
     * prev==0：返回 null（页面可展示 "--"）
     */
    public static Double percentChange(double current, double prev) {
        if (prev == 0D) return null;

        BigDecimal cur = BigDecimal.valueOf(current);
        BigDecimal pre = BigDecimal.valueOf(prev);

        BigDecimal percent = cur.subtract(pre)
                .divide(pre, 6, RoundingMode.HALF_UP) // 先多留几位，避免中间精度丢失
                .multiply(BigDecimal.valueOf(100))
                .setScale(2, RoundingMode.HALF_UP);   // 最终保留 2 位

        return percent.doubleValue();
    }

}


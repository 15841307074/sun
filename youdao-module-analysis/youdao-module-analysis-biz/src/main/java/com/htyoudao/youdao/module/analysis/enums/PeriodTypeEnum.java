package com.htyoudao.youdao.module.analysis.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 周期类型枚举
 *
 * <p>对应 {@code analysis_user_store_snapshot.period_type} 字段。
 *
 * <p>人和门店的快照分周、月两套：
 * 周快照每周一跑，月快照每月 1 号跑。
 * 因为月初可能恰好是周一，只用 period_start 无法区分是周快照还是月快照，
 * 所以加了这个字段。
 */
@Getter
@AllArgsConstructor
public enum PeriodTypeEnum {

    /**
     * 周快照：每周一凌晨跑出的上一周快照，type = 1
     */
    WEEK(1, "周"),

    /**
     * 月快照：每月 1 号凌晨跑出的上月快照，type = 2
     */
    MONTH(2, "月");

    /**
     * 类型值，存入数据库的 TINYINT
     */
    private final Integer type;

    /**
     * 描述，仅用于日志 / 展示
     */
    private final String description;

    /**
     * 按 type 值反查枚举。
     *
     * @param type 数据库里存的 TINYINT 类型值
     * @return 匹配到的枚举；找不到返回 {@code null}
     */
    public static PeriodTypeEnum valueOfType(Integer type) {
        return Arrays.stream(values())
                .filter(e -> e.getType().equals(type))
                .findFirst()
                .orElse(null);
    }
}

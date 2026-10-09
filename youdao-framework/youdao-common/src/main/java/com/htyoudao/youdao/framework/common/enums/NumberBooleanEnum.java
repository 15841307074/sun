package com.htyoudao.youdao.framework.common.enums;

public enum NumberBooleanEnum {
    // 枚举实例：1对应true(是)，0对应false(否)
    TRUE(1, true, "是"),
    FALSE(0, false, "否");

    // 数字值（1或0）
    private final int numberValue;
    // 布尔值（true或false）
    private final boolean booleanValue;
    // 描述信息
    private final String description;

    // 构造方法
    NumberBooleanEnum(int numberValue, boolean booleanValue, String description) {
        this.numberValue = numberValue;
        this.booleanValue = booleanValue;
        this.description = description;
    }

    // 获取数字值
    public int getNumberValue() {
        return numberValue;
    }

    // 获取布尔值
    public boolean getBooleanValue() {
        return booleanValue;
    }

    // 获取描述信息
    public String getDescription() {
        return description;
    }

    // 根据数字值获取枚举实例
    public static NumberBooleanEnum fromNumber(int number) {
        for (NumberBooleanEnum enumValue : values()) {
            if (enumValue.numberValue == number) {
                return enumValue;
            }
        }
        throw new IllegalArgumentException("无效的数字值: " + number + "，只能是0或1");
    }

    // 根据布尔值获取枚举实例
    public static NumberBooleanEnum fromBoolean(boolean bool) {
        return bool ? TRUE : FALSE;
    }
}

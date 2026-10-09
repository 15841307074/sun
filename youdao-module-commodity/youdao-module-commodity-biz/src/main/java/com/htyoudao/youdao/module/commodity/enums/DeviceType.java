package com.htyoudao.youdao.module.commodity.enums;

public enum DeviceType {
    // 定义枚举常量，分别对应APP和PC，关联对应的值和描述
    APP(0, "APP"),
    PC(1, "PC");

    // 枚举对应的值
    private final int value;
    // 枚举的描述
    private final String description;

    // 构造方法
    DeviceType(int value, String description) {
        this.value = value;
        this.description = description;
    }

    // 获取值
    public int getValue() {
        return value;
    }

    // 获取描述
    public String getDescription() {
        return description;
    }

    // 根据值获取对应的枚举
    public static DeviceType fromValue(int value) {
        for (DeviceType deviceType : DeviceType.values()) {
            if (deviceType.value == value) {
                return deviceType;
            }
        }
        throw new IllegalArgumentException("无效的DeviceType值: " + value);
    }
}

package com.htyoudao.youdao.module.commodity.enums;

/**
 * 商品套餐类型枚举
 */
public enum CommodityPackageType {

    FIXED_COMBO(1, "固定搭配套餐"),
    GROUP_SELECTABLE(2, "分组可选套餐"),
    SINGLE_ITEM(3, "单品");

    private final int code;
    private final String description;

    CommodityPackageType(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public int getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    /**
     * 根据 code 获取对应枚举
     * @param code 类型编码
     * @return 匹配的枚举，未找到时返回 null
     */
    public static CommodityPackageType fromCode(int code) {
        for (CommodityPackageType type : values()) {
            if (type.code == code) {
                return type;
            }
        }
        return null;
    }

    /**
     * 根据 code 获取枚举，未找到时抛出异常
     * @param code 类型编码
     * @return 匹配的枚举
     * @throws IllegalArgumentException 当 code 无效时
     */
    public static CommodityPackageType fromCodeStrict(int code) {
        CommodityPackageType type = fromCode(code);
        if (type == null) {
            throw new IllegalArgumentException("无效的套餐类型编码: " + code);
        }
        return type;
    }
}

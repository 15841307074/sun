package com.htyoudao.youdao.module.commodity.enums;

public enum SpuEnum {

    FIXED_COMBO(1, "固定搭配套餐"),
    GROUP_SELECTABLE(2, "分组可选套餐"),
    WX(1, "微信"),
    DCJ(2, "点餐机"),
    UP(1, "上架"),
    DOWN(0, "下架"),
    COLLOCATION(1, "固定搭配"),
    OPTIONAL(2, "分组可选"),
    FREE(1, "分组内自由可选"),
    FIXED(2, "分组内固定"),
    SURCHARGE_GROUP(3, "加价组"),
    AFFIRMATIVELY(1, "子品必选"),
    DISCRETIONARY(0, "子品不必选"),
    MULTIPLE_SELECTION(1, "单品可以多选"),
    SINGLE_SELECTION(0, "单品不能多选"),
    SHELF_UNLOCK(0, "上架锁未锁定"),
    SHELF_LOCK(1, "上架锁已锁定");


    private final Integer code;
    private final String description;

    SpuEnum(Integer code, String description) {
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
    public static SpuEnum fromCode(Integer code) {
        for (SpuEnum type : values()) {
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
    public static SpuEnum fromCodeStrict(Integer code) {
        SpuEnum type = fromCode(code);
        if (type == null) {
            throw new IllegalArgumentException("无效的套餐类型编码: " + code);
        }
        return type;
    }
}

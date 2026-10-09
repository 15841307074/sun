package com.htyoudao.youdao.module.system.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 损耗类型
 */
@Getter
@AllArgsConstructor
public enum ChannelType {

    ELE_ME("ELE_ME", "饿了么"),

    SAN_KUAI("SAN_KUAI", "美团"),
;

    private final String code;
    private final String description;



    /**
     * 根据代码获取枚举
     * @param code 类型代码
     * @return 对应的枚举值，如果找不到返回null
     */
    public static ChannelType getByCode(String code) {
        if (code == null) {
            return null;
        }
        for (ChannelType type : ChannelType.values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        return null;
    }



    @Override
    public String toString() {
        return this.description;
    }
}

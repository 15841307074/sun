package com.htyoudao.youdao.module.promotion.enums;

import lombok.Getter;

@Getter
public enum AdvertisingStoreEnum {



    ADVERTISING_STORE(111111111111111111L,"全部门店"),
    ;




    AdvertisingStoreEnum(Long status, String name) {
        this.status = status;
        this.name = name;
    }

    /**
     * 状态值
     */
    private final Long status;
    /**
     * 状态名
     */
    private final String name;

}

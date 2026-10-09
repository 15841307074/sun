package com.htyoudao.youdao.module.system.enums.org;

/**
 * 门店用户类型枚举
 * @author dht
 */
public interface StoreUserTypeConstants {
    /**
     * 负责人
     */
    Integer STORE_USER_TYPE_PRINCIPAL = 1;

    /**
     * 普通关系
     */
    Integer STORE_USER_TYPE_NORMAL = 0;

    /**
     * 门店  用户不可见
     */
    Integer STORE_USER_NOT_VISIBLE = 0;

    /**
     * 门店  用户可见
     */
    Integer STORE_USER_VISIBLE = 1;
}

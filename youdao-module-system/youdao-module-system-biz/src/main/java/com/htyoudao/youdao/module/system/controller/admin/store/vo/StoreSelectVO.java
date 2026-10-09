package com.htyoudao.youdao.module.system.controller.admin.store.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author zhangjihe
 */
@Data
public class StoreSelectVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 736536334535101649L;

    /**
     * 门店id
     */
    private Long storeId;

    /**
     * 门店名称
     */
    private String storeName;

    /**
     * 商家id
     */
    private Long businessId;

    /**
     * 商家名称
     */
    private String businessName;
}

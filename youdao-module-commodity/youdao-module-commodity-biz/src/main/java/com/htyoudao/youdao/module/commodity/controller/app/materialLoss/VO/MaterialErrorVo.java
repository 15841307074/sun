package com.htyoudao.youdao.module.commodity.controller.app.materialLoss.VO;

import lombok.Data;

@Data
public class MaterialErrorVo {

    //skuID
    private Long skuId;

    private Integer errorCode;

    private String errorName;

    private String commodityCode;

    //门店ID
    private Long storeId;

    //配方id
    private Long recipeId;
}

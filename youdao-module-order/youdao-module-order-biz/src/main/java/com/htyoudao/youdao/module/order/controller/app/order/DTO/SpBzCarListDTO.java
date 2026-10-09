package com.htyoudao.youdao.module.order.controller.app.order.DTO;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * 接收小程序购物车中的CarList数据
 * JavaJVM  2024/03/13
 */
@Data
public class SpBzCarListDTO implements Serializable {
    @Serial
    private static final Long serialVersionUID = -3863155173072819517L;
    /**
     * 小程序用户 id
     */
    private String openId;

    /**
     * 商品ID
     */
    private String commodityId;

    /**
     * 门店ID
     */
    private String storeId;

    /**
     * 商品数量
     */
    private Integer copies;

    /**
     * 商品规格
     */
    private List<String> skuCode = new ArrayList<>();

    /**
     * 是否是单品 (1 是，0 否)
     */
    private Integer isSingle;

    /**
     * 套餐里单品商品的 id
     */
    private List<String> commodityGroupSingleIdlist = new ArrayList<>();

    /**
     * 套餐里分组id
     */
    private List<String> commodityGroupIdList = new ArrayList<>();

    /**
     * 商品分组
     */
    private List<String> spuGroupFirstSing = new ArrayList<>();

    /**
     * 商品缩略图
     */
    private String thumbnailUrl;

    /**
     * 商品名称
     */
    private String commodityName;

    /**
     * 售卖价格
     */
    private BigDecimal price;

    /**
     * 售卖价格
     */
    private BigDecimal singlePrice;

    /**
     * 小料IDs
     */
    private List<String> condimentIds = new ArrayList<>();

    /**
     * 小料信息
     */
    private List<SpOrderCondimentInfoDTO> condimentInfos = new ArrayList<>();

    /**
     * 属性名称
     */
    private List<SpBzFlavorWithUserDTO> flavorWithUser = new ArrayList<>();

    /**
     * 商品划线价
     */
    private Integer spuStrikeThroughPrice;

    /**
     * 商品加料
     */
    private String condimentStr;

    /**
     * 小程序专门需要的数组.包含  spuGroupFirstSing   flavorWithUser  condimentStr
     */
    private List<String> spuFlavorCond = new ArrayList<>();

    private Object commodityFlavorList;

    private String skuStr;

    private String skuName;

    private Object options;

    private Object arr;

    private String isPurchase;

    private String afterId;

    private String unEqualId;
}

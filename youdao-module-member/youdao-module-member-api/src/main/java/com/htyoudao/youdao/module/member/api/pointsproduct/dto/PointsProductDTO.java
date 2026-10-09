package com.htyoudao.youdao.module.member.api.pointsproduct.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author dht
 */
@Data
public class PointsProductDTO implements Serializable {
    /**
     * 商品 id
     */
    private Long productId;

    /**
     * 商品名称
     */
    private String productName;


    /**
     * 商品类型 1 优惠劵 2 实体积分商品
     */
    private Integer productType;

    /**
     * 商品价格
     */
    private Long productPrice;

    /**
     * 小程序上下架
     */
    private Integer isAvailable;


    /**
     * 商品详情图
     */
    private String productDetailImages;

    /**
     * 优惠卷编号
     */
    private String couponCode;

    /**
     * 是否需要快递 1 是 2否
     */
    private Integer requiresShipping;

    /**
     * 商品状态 1.已售罄 2正常
     */
    private Integer productStatus;

    /**
     * 商品库存
     */
    private Integer productInventory;

    /**
     * 商品分类ID
     */
    private Long productCategoryId;
}

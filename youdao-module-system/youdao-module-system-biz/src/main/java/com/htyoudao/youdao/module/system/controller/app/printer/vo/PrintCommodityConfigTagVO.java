package com.htyoudao.youdao.module.system.controller.app.printer.vo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

/**
 * 该类代表整个打印配置，包含了不同角色（如店铺、会员、厨房、配送）的打印信息。
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class PrintCommodityConfigTagVO {
    /**
     * 商品信息---字体
     */
    private String productSizeTagStart;
    /**
     * 商品信息---粗细
     */
    private String productBoldTagStart;
    /**
     * 商品信息---字体
     */
    private String productSizeTagEnd;
    /**
     * 商品信息---粗细
     */
    private String productBoldTagEnd;
    /**
     * 商品信息---排序
     */
    private int productSort;

    /**
     * 商品详细信息---字体
     */
    private String productDetailSizeTagStart;
    /**
     * 商品详细信息---粗细
     */
    private String productDetailBoldTagStart;
    /**
     * 商品详细信息---字体
     */
    private String productDetailSizeTagEnd;
    /**
     * 商品详细信息---粗细
     */
    private String productDetailBoldTagEnd;

    /**
     * 加购信息---字体
     */
    private String productAddSizeTagStart;
    /**
     * 加购信息---粗细
     */
    private String productAddBoldTagStart;
    /**
     * 加购信息---字体
     */
    private String productAddSizeTagEnd;
    /**
     * 加购信息---粗细
     */
    private String productAddBoldTagEnd;
    /**
     * 加购信息---排序
     */
    private int productAddSort;
    /**
     * 活动信息---字体
     */
    private String productActivitySizeTagStart;
    /**
     * 活动信息---粗细
     */
    private String productActivityBoldTagStart;
    /**
     * 活动信息---字体
     */
    private String productActivitySizeTagEnd;
    /**
     * 活动信息---粗细
     */
    private String productActivityBoldTagEnd;

    /**
     * 优惠信息---字体
     */
    private int otherSizeTag;
}
package com.htyoudao.youdao.module.order.controller.admin.order.vo.commodity;

import com.htyoudao.youdao.module.order.controller.admin.order.vo.SingleDetailsDTO;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

@Data
public class CommodityVO implements Serializable {

    private static final long serialVersionUID = -2964136902144210150L;

    /**
     * 关联商品表
     */
    private Long commodityId;

    /**
     * 商品名称
     */
    private String commodityName;

    /**
     * 售卖价格
     */
    private BigDecimal price;

    /**
     * 商品缩略图
     */
    private String thumbnailUrl;

    /**
     * 属性名称
     */
    private String flavorName;

    /**
     * 属性
     */
    List<String> flavorids;

    /**
     * 商品规格
     */
    private String skuCode;

    /**
     * 购买数量
     */
    private Long copies;

    /**
     * 购物车id
     */
    private Long cartId;

    /**
     * 加料信息
     */
    List<CommodityCondimentsVO> condimentList;

    /**
     * 小料ID
     */
    private String condimentId;

    /**
     * 套餐单品信息
     */
    List<SingleDetailsDTO> singleDetailList;

    /**
     * 套餐里单品商品的 id
     */
    private List<Integer> commodityGroupSingleIdlist;
}

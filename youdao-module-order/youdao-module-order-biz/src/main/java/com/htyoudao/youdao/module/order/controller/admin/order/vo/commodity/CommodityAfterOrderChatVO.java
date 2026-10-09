package com.htyoudao.youdao.module.order.controller.admin.order.vo.commodity;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
public class CommodityAfterOrderChatVO implements Serializable {
    private static final long serialVersionUID = 8139305308615639179L;

    /**
     * 订单生成后加购商品ID
     */
    private Long afterId;

    /**
     * 商品ID
     */
    private Long commodityId;

    /**
     * 商品缩略图
     */
    private String thumbnailUrl;

    /**
     * 商品名称
     */
    private String commodityName;

    /**
     * 加购价格
     */
    private BigDecimal afterPrice;

    /**
     * 划线价格
     */
    private BigDecimal strikeThroughPrice;

    /**
     * 商品状态 (1: 正常, 2: 售罄, 3: 异常)
     */
    private Integer commodityStatus;

    List<String> skuCode = new ArrayList<>();

}

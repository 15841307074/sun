package com.htyoudao.youdao.module.order.controller.app.order.DTO;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 小料对象 commodity_condiments
 *
 * @author Qizhongnan
 * @date 2024-01-16
 */
@Data
public class CommodityCondimentsDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 小料ID
     */

    private Long condimentId;

    /**
     * 小料名称
     */
    private String condimentName;

    /**
     * 小料描述
     */
    private String description;

    /**
     * 价格
     */
    private BigDecimal price;

    private Integer number;

    /**
     * 图片地址
     */
    private String imageUrl;

    /**
     * 规格
     */
    private String commoditySkuname;

    /**
     * 加料
     */
    private String commodityFeedingname;

    /**
     * 关联商品 id
     */
    private Long commodityId;

    /**
     * 小料ID批量操作
     */
    private String condimentIdIn;

    private String remark;

}

package com.htyoudao.youdao.module.order.controller.app.order.DTO;

import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 商品属性对象 commodity_flavor
 *
 * @author qzn
 * @date 2024-01-20
 */
@Data
public class CommodityFlavorDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 属性ID
     */
    private Long flavorId;

    /**
     * 商品ID
     */
    private Long commodityId;

    /**
     * 属性名称
     */
    private String flavorName;

    /**
     * 属性明细 list
     */
    private List<String> flavorValueList = new ArrayList<>();

    /**
     * 属性值列表，以逗号分隔
     */
    private String flavorValue;

    /**
     * 逻辑删除标识，0表示正常，2表示删除
     */
    private Long delFlag;

    /**
     * 属性类型 1 是单选 2 是多选
     */
    private Integer flavorType;
}

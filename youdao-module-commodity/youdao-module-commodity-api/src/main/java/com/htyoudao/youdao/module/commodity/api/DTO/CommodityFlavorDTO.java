package com.htyoudao.youdao.module.commodity.api.DTO;


import lombok.Data;
import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 商品属性对象 commodity_flavor
 *
 * @author qzn
 * @date 2024-01-20
 */
@Data
public class CommodityFlavorDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 2232328331862145175L;

    /**
     * 属性ID 弃用
     */
    private Long flavorId;

    /**
     * 商品ID 弃用
     */
    private Long commodityId;

    /**
     * 属性名称
     */
    private String flavorName;

    /**
     * key 属性值 value 属性是否上下架 1 上架 0 下架
     */
    private List<Map<String, Integer>> flavorValueListMap = new ArrayList<>();

    /**
     * 属性值列表，以逗号分隔 弃用
     */
    private String flavorValue;

    /**
     * 属性类型 1 是单选 2 是多选 弃用
     */
    private Integer flavorType;
}

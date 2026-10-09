package com.htyoudao.youdao.module.commodity.dal.dataobject;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

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
public class CommodityFlavor {
    private static final long serialVersionUID = 1L;

    /**
     * 属性ID 弃用
     */
    @TableId(value = "flavor_id", type = IdType.ASSIGN_ID)
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

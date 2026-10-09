package com.htyoudao.youdao.module.commodity.api.DTO;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Data
public class FlavorDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 7595547948068069414L;

    private Long  flavorId;
    private String flavorName;
    private List<String>  flavorValues;
    /**
     * key 属性值 value 属性是否上下架 1 上架 0 下架
     */
    private List<Map<String, Integer>> flavorValueListMap = new ArrayList<>();
    //顺序
    Integer sort;
}

package com.htyoudao.youdao.module.commodity.api.DTO;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 商品隐藏状态过滤信息。
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CommodityHiddenFilterDTO implements Serializable {

    /** 商品 ID 集合。 */
    private List<Long> commodityIds = new ArrayList<>();

    /** 集合中的商品是否为隐藏商品：1 是，0 否。 */
    private Integer isHidden;
}

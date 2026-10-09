package com.htyoudao.youdao.module.commodity.api.VO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 活动商品
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CommodityActivityVO implements Serializable {
    private static final long serialVersionUID = 1L;

    private List<Long> commodityIds;
}

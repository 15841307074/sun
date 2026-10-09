package com.htyoudao.youdao.module.commodity.api.DTO;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import com.htyoudao.youdao.module.commodity.api.DTO.FlavorDto;

@Data
public class SingleDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 4963165300739106614L;

    private Long singleId;
    private Long commodityId;
    private String singleName;
    private String imageUrl;
    private BigDecimal upPrice;
    /**是否默认选中 0否 1是*/
    private int defaultChoose;
    /**是//是否必选 0否 1是*/
    private int requiredChoose;

    private String skuName;
    /**
     * 门店单品上下架状态
     * 0下架 1下架
     */
    private int commodityStoreSingleStatus;
    /**
     * 门店下套餐里单品的份数
     */
    private Integer commodityStoreSingleCopies;
    //顺序
    Integer sort;

    /** 小程序上下架状态 1 上架 0 下架*/
    private Integer wxStatus;

    /** 门店上下架状态 1 上架 0 下架*/
    private Integer storeStatus;

    @Schema(description = "所在套餐的spuID")
    private Long commodityStoreSpuId;

    @Schema(description = "当前Single的连锁库skuId")
    private Long singleSkuId;

    @Schema(description = "属性集合")
    private List<FlavorDto> commodityFlavors = new ArrayList<>();
}
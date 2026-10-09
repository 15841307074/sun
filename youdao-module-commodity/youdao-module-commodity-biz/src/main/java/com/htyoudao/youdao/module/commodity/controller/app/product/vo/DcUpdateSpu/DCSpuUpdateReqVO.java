package com.htyoudao.youdao.module.commodity.controller.app.product.vo.DcUpdateSpu;


import com.htyoudao.youdao.module.commodity.dal.dataobject.CommodityCondiments;
import com.htyoudao.youdao.module.commodity.dal.dataobject.CommodityFlavor;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Schema(description = "DC -点餐机修改商品  Request VO")
@Data
public class DCSpuUpdateReqVO {
    @Schema(description = "门店下商品 id")
    @NotNull(message = "门店下商品 id不能为空")
    private Long commodityStoreSpuId;

    /**
     * 门店下商品分类的ID
     */
    @Schema(description = "门店下商品分类id")
    @NotNull(message = "门店下商品分类id不能为空")
    private Long commodityStoreCategoryId;



    @Schema(description = "门店下商品 id")
    @NotNull(message = "门店id不能为空")
    private Long storeId;

    @Schema(description = "是否单品")
    @NotNull(message = "是否单品不能为空")
    private Integer  commodityStoreSpuIsSingle;;

    @Schema(description = "商品里的属性集合", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<CommodityFlavor> flavorList = new ArrayList<>();


    @Schema(description = "商品里所有添加的小料", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<CommodityCondiments> condimentsList  =  new ArrayList<>();

    @Schema(description = "门店分组集合")
    private List<DCGroupUpdateReqVO> groupList = new ArrayList<>();

    @Schema(description = "门店下规格集合")
    private List<DCSkuUpdateReqVO> skuList = new ArrayList<>();

    private Long loginUserId ;
}

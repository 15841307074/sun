package com.htyoudao.youdao.module.commodity.controller.admin.storeCategory.VO;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.htyoudao.youdao.module.commodity.dal.dataobject.TimeBase;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "管理后台 - 门店分类查询逻辑 Request VO")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CommodityStoreCategoryRespVO extends TimeBase {

    /**
     * 门店下商品分类ID
     */
    @Schema(description = "门店下分类的唯一ID")
    private Long commodityStoreCategoryId;

    /**
     * 门店ID
     */
    @Schema(description = "门店ID")
    private Long storeId;

    /**
     * 类型  是否下单必选分组 1 是 0否
     */
    @Schema(description = " 类型  是否下单必选分组 1 是 0否")
    private Integer type;

    /**
     * 门店商品分类URL
     */
    @Schema(description = " 门店商品分类URL")
    private String commodityStoreCategoryImage;

    /**
     * 套餐原始 ID
     */
    @Schema(description = " 套餐原始 ID")
    private Long commodityStorePrimitiveCategoryId;

    /**
     * 门店商品分类名称
     */
    @Schema(description = " 门店商品分类名称")
    private String commodityStoreCategoryName;

    /**
     * 门店商品分类状态
     */
    @Schema(description = " 门店商品分类状态")
    private Integer commodityStoreCategoryStatus;

    /**
     * 门店下商品是否锁
     */
    @Schema(description = " 门店下商品是否锁")
    private Boolean commodityStoreCategoryLock;

    /**
     * 门店下商品分类的顺序
     */
    @Schema(description = " 门店下商品分类的顺序")
    private Integer commodityStoreCategorySort ;



    @Schema(description = "商品数量")
    private Integer commodityQuantity;

}

package com.htyoudao.youdao.module.commodity.controller.admin.storeSpu.VO;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.htyoudao.youdao.module.commodity.dal.dataobject.CommodityStoreSpu;
import com.htyoudao.youdao.module.commodity.dal.dataobject.TimeBase;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Schema(description = "管理后台 - 门店分类修改 Request VO")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class StoreCategoryUpdateReqVO extends TimeBase implements Serializable {


    private static final long serialVersionUID = 1L;

    @Schema(description = "门店下商品分类ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "门店下商品分类ID不能为空")
    private Long commodityStoreCategoryId;


    @Schema(description = "门店ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "门店ID不能为空")
    private Long storeId;


    @Schema(description = "类型  是否下单必选分组 1 是 0否", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer type;


    @Schema(description = "门店商品分类URL", requiredMode = Schema.RequiredMode.REQUIRED)
    private String commodityStoreCategoryImage;

    @Schema(description = "套餐原始 ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long commodityStorePrimitiveCategoryId;


    @Schema(description = "门店商品分类名称", requiredMode = Schema.RequiredMode.REQUIRED)
    private String commodityStoreCategoryName;

    @Schema(description = "门店商品分类状态", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer commodityStoreCategoryStatus;

    @Schema(description = "门店下商品是否锁", requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean commodityStoreCategoryLock;


    @Schema(description = "门店下商品分类的顺序", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer commodityStoreCategorySort = 0;


}

package com.htyoudao.youdao.module.commodity.controller.admin.storeSpu.VO;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.htyoudao.youdao.module.commodity.dal.dataobject.TimeBase;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
@Schema(description = "管理后台 - 门店分类修改 Request VO")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CommodityStoreCategoryUpReqVO extends TimeBase implements Serializable {

    /**
     * 门店下商品分类ID
     */
    @Schema(description = "分类ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "分类ID不能为空")
    private Long commodityStoreCategoryId;

    /**
     * 门店ID
     */
    @Schema(description = "门店ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "门店ID不能为空")
    private Long storeId;

    /**
     * 类型  是否下单必选分组 1 是 0否
     */
    @Schema(description = "类型  是否下单必选分组 1 是 0否", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "类型  是否下单必选分组 1 是 0否不能为空")
    private Integer type;

    /**
     * 门店商品分类URL
     */
    @Schema(description = "门店商品分类URL")
    @NotNull(message = "门店商品分类URL不能为空")
    private String commodityStoreCategoryImage;

    /**
     * 分类原始 ID
     */
    @Schema(description = "分类原始 ID")
    @NotNull(message = "分类原始 ID不能为空")
    private Long commodityStorePrimitiveCategoryId;

    /**
     * 分类名称
     */
    @Schema(description = "分类名称")
    @NotNull(message = "分类名称不能为空")
    private String commodityStoreCategoryName;

    /**
     * 分类状态
     */
    @Schema(description = "分类状态")
    @NotNull(message = "分类状态不能为空")
    private Integer commodityStoreCategoryStatus;

    /**
     * 分类是否锁
     */
    @Schema(description = "分类是否锁")
    private Boolean commodityStoreCategoryLock;

    /**
     * 分类的顺序
     */
    @Schema(description = "分类的顺序")
    @NotNull(message = "分类的顺序不能为空")
    private Integer commodityStoreCategorySort ;

}

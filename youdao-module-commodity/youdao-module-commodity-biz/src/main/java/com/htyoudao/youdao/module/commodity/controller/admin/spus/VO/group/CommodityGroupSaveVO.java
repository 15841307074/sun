package com.htyoudao.youdao.module.commodity.controller.admin.spus.VO.group;

import com.htyoudao.youdao.module.commodity.controller.admin.spus.VO.single.CommoditySingleSaveVO;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
@Data
public class CommodityGroupSaveVO {

    /** 排序 */
    @Schema(description = "排序",requiredMode = Schema.RequiredMode.REQUIRED)
    private int sort;

    /** 分组名称 */
    @Schema(description = "分组名称",requiredMode = Schema.RequiredMode.REQUIRED)
    private String commodityGroupName;

    /** 必选商品数量 */
    @Schema(description = "必选商品数量",requiredMode = Schema.RequiredMode.REQUIRED)
    private Long choose;

    /** 分组属性(本组商品是否包含以下所有单品) 1 可选 2 固定 3 加价组 */
    @Schema(description = "分组属性(本组商品是否包含以下所有单品) 1 可选 2 固定 3 加价组",requiredMode = Schema.RequiredMode.REQUIRED)
    private int attribute;

    /**
     * 分组状态 1上 0 下
     */
    @Schema(description = "分组状态 1上 0 下",requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer status;
    /**
     * 门店下套餐分组里可选的数量
     */
    @Schema(description = "门店下套餐分组里可选的数量",requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer commodityStoreGroupChoose;
    /**
     * 同一商品是否可选多份 1 是 0否
     */
    @Schema(description = "同一商品是否可选多份 1 是 0否",requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer chooseMany;

    @Schema(description = "分组里子品列表",requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "分组里子品列表不能为空")
    private List<CommoditySingleSaveVO> commoditySingleSaveVOList = new ArrayList<>();
}

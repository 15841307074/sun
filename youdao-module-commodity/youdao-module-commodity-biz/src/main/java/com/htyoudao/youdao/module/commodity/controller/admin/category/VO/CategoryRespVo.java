package com.htyoudao.youdao.module.commodity.controller.admin.category.VO;

import com.htyoudao.youdao.module.commodity.controller.admin.spus.VO.spu.CommoditySpusByIdRespVo;
import com.htyoudao.youdao.module.commodity.dal.dataobject.TimeBase;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Schema(description = "管理后台 - 商品查询列表 Response VO")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CategoryRespVo extends TimeBase {


    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long id;

    @Schema(description = "类型 是否下单必选分组 1 是 0否", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer type;

    @Schema(description = "分类名称", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @Schema(description = "顺序", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer sort;

    @Schema(description = "分类状态 0:启用，1:禁用", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer status;

    @Schema(description = "是否隐藏 1是 0否", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer isHidden;

    @Schema(description = "图片地址")
    private String url;

    @Schema(description = "字典键值")
    private String dictValue;


    @Schema(description = "分类里记录所有分类下商品的 ids")
    private String commoditySpusIds;

    @Schema(description = "商品列表")
    private List<CommoditySpusByIdRespVo> commoditySpusList = new ArrayList<>();

    /**
     * 商品数量
     */
    @Schema(description = "商品数量")
    private Integer commodityNum;
}

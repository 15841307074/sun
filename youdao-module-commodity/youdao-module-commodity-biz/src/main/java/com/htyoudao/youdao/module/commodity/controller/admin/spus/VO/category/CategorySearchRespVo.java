package com.htyoudao.youdao.module.commodity.controller.admin.spus.VO.category;

import com.htyoudao.youdao.module.commodity.dal.dataobject.CommoditySpus;
import com.htyoudao.youdao.module.commodity.dal.dataobject.TimeBase;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Schema(description = "管理后台 - 模糊查询分类列表 Response VO")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CategorySearchRespVo extends TimeBase {

    @Schema(description = "ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long id;

    @Schema(description = "类型 是否下单必选分组 1 是 0否", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer type;

    @Schema(description = "分类名称", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @Schema(description = "顺序", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer sort;

    @Schema(description = "分类状态 0:启用，1:禁用", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer status;

    @Schema(description = "图片地址")
    private String url;

    @Schema(description = "字典键值")
    private String dictValue;

    @Schema(description = "分类里记录所有分类下商品的 ids")
    private String commoditySpusIds;


    @Schema(description = "分类里商品列表")
    private List<CommoditySpus> commoditySpusList = new ArrayList<>();

    @Schema(description = "是否单品")
    private Integer isSingle;

    @Schema(description = "商品数量")
    private Integer commodityNum;

    @Schema(description = "查询逻辑 1全部 2 上架 3下架")
    private Integer selectView;

}

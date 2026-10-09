package com.htyoudao.youdao.module.commodity.controller.admin.spus.VO.spu;

import com.htyoudao.youdao.module.commodity.dal.dataobject.TimeBase;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Schema(description = "管理后台 - 查询商品通用 Request VO")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CommoditySpusQueryReqVo extends TimeBase {

    @Schema(description = "商品ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long commodityId;

    @Schema(description = "商品名称", requiredMode = Schema.RequiredMode.REQUIRED)
    private String commodityName;

    @Schema(description = "关联商品分类 ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "关联商品分类ID不能为空")
    private Long categoryId;

    @Schema(description = "商品描述")
    private String description;

    /** 商品顺序 */
    @Schema(description = "商品顺序", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer sort;

    @Schema(description = "是否是单品 1是 0否",requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer isSingle;

    @Schema(description = "门店上下架状态 1 上架 2 下架",requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer storeStatus;

    @Schema(description = "小程序上下架状态 1 上架 2 下架",requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer wxStatus;


    @Schema(description = "商品所属分组名")
    private String categoryName;

    @Schema(description = "查询逻辑 1全部 2 上架 3下架",requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer selectView;

    @Schema(description = "1 wx 2dcj",requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer chooseView;

    @Schema(description = "批量规则 1上架 2下架 3删除 4移动分组 5改可售时间",requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer sortingRules;

    @Schema(description = "商品列表")
    private List<Long> commodityIds = new ArrayList<>();

    @Schema(description = "图片地址",requiredMode = Schema.RequiredMode.REQUIRED)
    private List<String> imageUrlVO;

    @Schema(description = "是否隐藏 1是 0否")
    private Integer isHidden;



}

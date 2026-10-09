package com.htyoudao.youdao.module.commodity.controller.admin.spus.VO.spu;

import com.htyoudao.youdao.module.commodity.controller.admin.spus.VO.group.CommodityGroupSaveVO;
import com.htyoudao.youdao.module.commodity.controller.admin.spus.VO.sku.CommoditySkuSaveVO;
import com.htyoudao.youdao.module.commodity.dal.dataobject.*;
import com.htyoudao.youdao.module.commodity.dal.dataobject.commodityTag.CommodityTag;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Schema(description = "管理后台 - 商品新增 Request VO")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CommoditySpusUpdateReqVo extends TimeBase{

    @Schema(description = "商品ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "商品ID不能为空")
    private Long commodityId;

    @Schema(description = "商品名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "商品名称不能为空")
    private String commodityName;

    @Schema(description = "关联商品分类 ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "商品分类不能为空")
    private Long categoryId;

    @Schema(description = "商品描述")
    private String description;

    /** 商品顺序 */
    @Schema(description = "商品顺序", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer sort;


    @Schema(description = "回显备注")
    private String dictValue;

    @Schema(description = "单位")
    private String unit;

    @Schema(description = "是否是单品 1是 0否",requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "是否是单品不能为空")
    private Integer isSingle;



    @Schema(description = "门店上下架状态 1 上架 2 下架",requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "门店上下架状态不能为空")
    private Integer storeStatus;


    @Schema(description = "小程序上下架状态 1 上架 2 下架",requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "小程序上下架不能为空")
    private Integer wxStatus;


    @Schema(description = "销量")
    private Long salesVolumes;


    @Schema(description = "商品所属分组名")
    private String categoryName;

    @Schema(description = "商品说明")
    @NotNull(message = "商品说明不能为空")
    private String goodsExplain;


    @Schema(description = "打包没多少份",requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer manyCopy;

    @Schema(description = "打包费（单位元）",requiredMode = Schema.RequiredMode.REQUIRED)
    private BigDecimal packageFee;

    @Schema(description = "售卖规则（0.正常售卖 1.仅套餐售卖 2.仅小料售卖 3.仅兑换售卖）",requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "售卖规则不能为空")
    private Integer saleRule;

    @Schema(description = "单点不送（0关闭  1开启）",requiredMode = Schema.RequiredMode.REQUIRED)
    private String spuDetailUrl;

    @Schema(description = "最少购买多少份",requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer limitBuyNumber;


    @Schema(description = "每天限购",requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer limitDayBuyNumber;

    @Schema(description = "每单限购",requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer limitOrderBuyNumber;

    @Schema(description = "套餐类型（1.固定搭配套餐，2.分组可选套餐）",requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer setmealType;

    @Schema(description = "适用人数",requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer applicableNumber;


    @Schema(description = "小料是否多选 1是 0否",requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer condimentIsMore;

    @Schema(description = "最多可加小料数量",requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer maxCondimentNumber;

    @Schema(description = "图片地址",requiredMode = Schema.RequiredMode.REQUIRED)
    private List<String> imageUrlVO= new ArrayList<>();

/*    @Schema(description = "商品标签集合",requiredMode = Schema.RequiredMode.REQUIRED)
    private List<CommodityTag> commodityTagList = new ArrayList<>();*/

    private List<Long> tagIdList = new ArrayList<>();

    /**商品里的属性集合 */
    @Schema(description = "商品里的属性集合",requiredMode = Schema.RequiredMode.REQUIRED)
    private List<CommodityFlavor> commodityFlavorList = new ArrayList<>();


    @Schema(description = "商品列表里所有添加的小料",requiredMode = Schema.RequiredMode.REQUIRED)
    private List<CommodityCondiments> commodityCondimentsList  =  new ArrayList<>();

    @Schema(description = "商品里sku列表",requiredMode = Schema.RequiredMode.REQUIRED)
    private List<CommoditySkuSaveVO> commoditySkusList = new ArrayList<>();

    @Schema(description = "套餐里分组添加的单品列表",requiredMode = Schema.RequiredMode.REQUIRED)
    private List<CommodityGroupSaveVO> commodityGroupList = new ArrayList<>();
}

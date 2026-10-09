package com.htyoudao.youdao.module.commodity.controller.admin.storeSpu.VO;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.htyoudao.youdao.module.commodity.dal.dataobject.CommodityStoreGroup;
import com.htyoudao.youdao.module.commodity.dal.dataobject.CommodityStoreSku;
import com.htyoudao.youdao.module.commodity.dal.dataobject.CommodityTemplateSkus;
import com.htyoudao.youdao.module.commodity.dal.dataobject.TimeBase;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
@Data
public class TemplateSpusRespVO extends TimeBase {



    @Schema(description = "模板商品ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long commodityTemplateId;

    @Schema(description = "商品id", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long commodityId;

    @Schema(description = "模板id", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long templateId;

    @Schema(description = "商品名称", requiredMode = Schema.RequiredMode.REQUIRED)
    private String commodityName;

    @Schema(description = "关联模板商品分类 ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long categoryTemplateId;

    @Schema(description = "商品描述", requiredMode = Schema.RequiredMode.REQUIRED)
    private String description;

    @Schema(description = "商品顺序", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long sort;


    @Schema(description = "图片地址", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<String> imageUrlList = new ArrayList<>();

    @Schema(description = "视频地址（弃用）", requiredMode = Schema.RequiredMode.REQUIRED)
    private String videoUrl;


    @Schema(description = "单位", requiredMode = Schema.RequiredMode.REQUIRED)
    private String unit;


    @Schema(description = "字典键值", requiredMode = Schema.RequiredMode.REQUIRED)
    private String dictValue;


    @Schema(description = "是否是单品（1是 2 否）", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer isSingle;

    @Schema(description = "商品详情图（弃用）", requiredMode = Schema.RequiredMode.REQUIRED)
    private String spuDetailUrl;

    @Schema(description = "商品关联 skuId（弃用）", requiredMode = Schema.RequiredMode.REQUIRED)
    private String skuIds;

    @Schema(description = "商品里小料的 ids（弃用）", requiredMode = Schema.RequiredMode.REQUIRED)
    private String condimentIds;


    @Schema(description = "商品缩略图（弃用）", requiredMode = Schema.RequiredMode.REQUIRED)
    private String thumbnailUrl;


    @Schema(description = "门店上下架状态 1 上架 2 下架", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer storeStatus;


    @Schema(description = "小程序上架状态 1 上架 2 下架", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer wxStatus;

    @Schema(description = "套餐里分组 ids（弃用）", requiredMode = Schema.RequiredMode.REQUIRED)
    private String groupIds;


    @Schema(description = "商品里记录的属性表的 ids（弃用）", requiredMode = Schema.RequiredMode.REQUIRED)
    private String flavorIds;


    @Schema(description = "销量", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer salesVolumes;


    @Schema(description = "小料 json结构", requiredMode = Schema.RequiredMode.REQUIRED)
    private String condiments;


    @Schema(description = "属性 json结构", requiredMode = Schema.RequiredMode.REQUIRED)
    private String flavor;


    @Schema(description = "分组名称", requiredMode = Schema.RequiredMode.REQUIRED)
    private String categoryName;


    @Schema(description = "商品标签", requiredMode = Schema.RequiredMode.REQUIRED)
    private String spusTag;


    @Schema(description = "打包费（单位元）", requiredMode = Schema.RequiredMode.REQUIRED)
    private BigDecimal packageFee;


    @Schema(description = "售卖规则（0.正常售卖 1.仅套餐售卖 2.仅小料售卖 3.仅兑换售卖）", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer saleRule;


    @Schema(description = "最少购买多少份", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer limitBuyNumber;


    @Schema(description = "每天限购", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer limitDayBuyNumber;


    @Schema(description = "每单限购", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer limitOrderBuyNumber;


    @Schema(description = "套餐类型（1.固定搭配套餐，2.分组可选套餐）", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer setmealType;


    @Schema(description = "适用人数", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer applicableNumber;

    @Schema(description = "", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer manyCopy;

    @Schema(description = "", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer condimentIsMore;

    @Schema(description = "", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer maxCondimentNumber;


    @TableField(exist = false)
    private int storeForceStatus;
    @TableField(exist = false)
    private int wxForceStatus;



    //最低价
    @TableField(exist = false)
    private BigDecimal lowPrice;
    //最高价
    @TableField(exist = false)
    private BigDecimal highPrice;

    //最低划线价
    @TableField(exist = false)
    private BigDecimal lowStrikePrice;
    //最高划线价
    @TableField(exist = false)
    private BigDecimal highStrikePrice;

    //是否多规格
    @TableField(exist = false)
    private Boolean isMoreSku;
    @TableField(exist = false)
    private List<CommodityTemplateSkus> commodityTemplateSkusList ;
    /**
     * 是否只允许修改价格 1 是 2 否
     */
    @TableField(exist = false)
    private Integer choosePrice;

}

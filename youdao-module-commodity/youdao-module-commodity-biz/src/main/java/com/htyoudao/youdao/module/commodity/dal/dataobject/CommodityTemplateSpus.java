package com.htyoudao.youdao.module.commodity.dal.dataobject;


import com.baomidou.mybatisplus.annotation.*;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;


import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * 模板下商品表
 * </p>
 *
 * @author ssz
 * @since 2025-02-05
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class CommodityTemplateSpus  extends TimeBase implements Serializable {

    private static final long serialVersionUID = 1L;
    /**
     * 模板商品ID
     */
    @TableId(value = "commodity_template_id", type = IdType.ASSIGN_ID)
    private Long commodityTemplateId;
    /**
     * 商品id
     */
    private Long commodityId;
    /**
     * 模板id
     */
    private Long templateId;

    /**
     * 商品名称
     */
    private String commodityName;

    /**
     * 关联模板商品分类 ID
     */
    private Long categoryTemplateId;

    /**
     * 商品描述
     */
    private String description;

    /**
     * 商品顺序
     */
    private Long sort;

    /**
     * 图片地址
     */
    private String imageUrl;

    /**
     * 视频地址（弃用）
     */
    private String videoUrl;

    /**
     * 单位
     */
    private String unit;


    /**
     * 字典键值
     */
    private String dictValue;

    /**
     * 是否是单品（1是 2 否）
     */
    private Integer isSingle;

    /**
     * 商品详情图（弃用）
     */
    private String spuDetailUrl;

    /**
     * 商品关联 skuId（弃用）
     */
    private String skuIds;

    /**
     * 商品里小料的 ids（弃用）
     */
    private String condimentIds;

    /**
     * 商品缩略图（弃用）
     */
    private String thumbnailUrl;

    /**
     * 门店上下架状态 1 上架 2 下架
     */
    private Integer storeStatus;

    /**
     * 小程序上架状态 1 上架 2 下架
     */
    private Integer wxStatus;

    /**
     * 套餐里分组 ids（弃用）
     */
    private String groupIds;

    /**
     * 商品里记录的属性表的 ids（弃用）
     */
    private String flavorIds;


    /**
     * 销量
     */
    private Integer salesVolumes;

    /**
     * 小料 json结构
     */
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private String condiments;

    /**
     * 属性 json结构
     */
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private String flavor;

    /**
     * 分组名称
     */
    private String categoryName;

    /**
     * 商品标签
     */
    private String spusTag;
    /**
     * 商品标签 Ids
     */
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private String tagIds;



    /**
     * 打包费（单位元）
     */
    private BigDecimal packageFee;

    /**
     * 售卖规则（0.正常售卖 1.仅套餐售卖 2.仅小料售卖 3.仅兑换售卖）
     */
    private Integer saleRule;

    /**
     * 最少购买多少份
     */
    private Integer limitBuyNumber;

    /**
     * 每天限购
     */
    private Integer limitDayBuyNumber;

    /**
     * 每单限购
     */
    private Integer limitOrderBuyNumber;

    /**
     * 套餐类型（1.固定搭配套餐，2.分组可选套餐）
     */
    private Integer setmealType;

    /**
     * 适用人数
     */
    private Integer applicableNumber;

    private Integer manyCopy;

    private Integer condimentIsMore;

    private Integer maxCondimentNumber;


    @Schema(description = "商品说明")
    private String goodsExplain;



}

package com.htyoudao.youdao.module.commodity.dal.dataobject;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * 商品对象 commodity_spus
 * 
 * @author Qizhongann
 * @date 2024-01-16
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class CommoditySpus extends TimeBase
{
    private static final long serialVersionUID = 1L;

    /** 商品ID */
    @TableId(value = "commodity_id",type= IdType.ASSIGN_ID)
    private Long commodityId;

    /** 商品名称 */
    private String commodityName;

    /** 关联商品分类 ID */
    private Long categoryId;


    /** 商品描述 */
    private String description;

    /** 商品顺序 */
    private Integer sort;

    /** 图片地址 */
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private String imageUrl;

    /** 视频地址 弃用*/
    private String videoUrl;

    /** 单点不送（0关闭  1开启）*/
    private String spuDetailUrl;


    /** 商品缩略图 弃用*/
    private String thumbnailUrl;



    /** 回显备注 */
    private String dictValue;

    /** 单位 */
    private String unit;



    /** 是否是单品 1是 0否 */
    private Integer isSingle;




    /** 商品表里 skuIds 弃用*/
    private String skuIds;




    /** 门店上下架状态 1 上架 2 下架*/
    private Integer storeStatus;




    /** 小程序上下架状态 1 上架 2 下架*/
    private Integer wxStatus;

    /** 是否隐藏 1是 0否 */
    private Integer isHidden;



    /** 商品里小料列表id后 弃用 */
    private String condimentIds;




    /**套餐里分组 ids 弃用*/
    private String groupIds;

    /** 商品说明 */
    private String goodsExplain;








    /** 商品里记录的属性表的 ids 弃用*/

    private String flavorIds;




    /**
     * 销量
     */
    private Long salesVolumes;

    /**
     * 小料 JSON 结构
     */
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private String condiments;

    /**
     * 属性 Json 结构
     */
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private String flavor;
    /**
     * 商品所属分类名
     */
    private String categoryName;

    /**
     * 商品标签（弃用）
     */
    private String spusTag;

    /**
     * 商品标签 ids
     */
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private String tagIds;



    /**
     * 打包没多少份
     */
    private Integer manyCopy;

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

    /**
     * 小料是否多选 1是 0否
     */
    private Integer condimentIsMore;

    /**
     * 最多可加小料数量
     */
    private Integer maxCondimentNumber;
    /**
     * 规格是否有变化 true 为有改变 false 为无变化
     */
    private Boolean isChange;
    /**
     * 规格配方时候全部设置完毕 1 完毕
     */
    private Integer skuFlag;

    /**
     * 商品是否上架锁定 1锁定 0未锁定
     */
    private Integer shelfLock;


}

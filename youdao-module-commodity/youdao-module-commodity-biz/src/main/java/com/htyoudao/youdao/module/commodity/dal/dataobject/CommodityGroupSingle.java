package com.htyoudao.youdao.module.commodity.dal.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;

import com.htyoudao.youdao.framework.mybatis.core.dataobject.BusinessBaseDO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 套餐里分组与单品关系表对象 commodity_group_single
 *
 * @author Qizhongnan
 * @date 2024-01-19
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class CommodityGroupSingle extends BusinessBaseDO {
    private static final long serialVersionUID = 1L;

    /**
     * 套餐分组与单品关系表ID
     */

    @TableId(value = "commodity_group_single_id", type = IdType.ASSIGN_ID)
    private Long commodityGroupSingleId;

    /**
     * 关联分组 id
     */
    private Long groupId;

    /**
     * 单品所属ID 字段标识不清晰 但生产已如此使用不更改
     */
    private Long spuId;

    /**
     * 单品所指的商品 Id 字段标识不清晰 但生产已如此使用不更改
     */
    private Long commodityId;


    /**
     * 排序
     */
    private int sort;

    /**
     * 加价
     */
    private BigDecimal upPrice;

    /**
     * 是否在分组中显示 (1 是, 2 否)
     */
    private int display;

    /**
     * 份数
     */
    private int copies;

    /**
     * 商品名称
     */
    private String commodityName;



    /**
     * 划线价格
     */
    private BigDecimal markingPrice;

    /**
     * 商品图片
     */
    private String commodityUrl;

    /**
     * 是否默认选中 1是 0否
     */
    private Integer defaultChoose;

    /**
     * 是否必选 0否 1是
     */
   private Integer  requiredChoose;


    /**
     * 此品的规格名称
     */
    private String singleSkuName;

    /**
     * 此品的规格 id
     */
    private Long singleSkuId;

    /** 小程序上下架状态 1 上架 0 下架*/
    private Integer wxStatus;

    /** 门店上下架状态 1 上架 0 下架*/
    private Integer storeStatus;

    /** 口味*/
    private String flavor;

}

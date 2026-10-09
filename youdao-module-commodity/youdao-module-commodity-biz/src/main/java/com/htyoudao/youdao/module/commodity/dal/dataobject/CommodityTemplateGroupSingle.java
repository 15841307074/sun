package com.htyoudao.youdao.module.commodity.dal.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.htyoudao.youdao.framework.mybatis.core.dataobject.BusinessBaseDO;
import lombok.*;

import java.math.BigDecimal;

/**
 * 套餐里分组与单品关系表对象 commodity_group_single
 *
 * @author Qizhongnan
 * @date 2024-01-19
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@EqualsAndHashCode(callSuper = true)
public class CommodityTemplateGroupSingle extends BusinessBaseDO {
    private static final long serialVersionUID = 1L;
    /**
     * 模板套餐分组与单品关系表ID
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;


    private Long templateId;

    private Long commodityTemplateId;

    private Long templateGroupId;

    /**
     * 套餐分组与单品关系表ID
     */
    private Long commodityGroupSingleId;

    /**
     * 单品ID
     */
    private Long commodityId;

    /**
     * 排序
     */
    private Integer sort;

    /**
     * 加价
     */
    private BigDecimal upPrice;

    /**
     * 是否在分组中显示 (1 是, 2 否)
     */
    private Integer display;

    /**
     * 份数
     */
    private Integer copies;


    /**
     * 划线价
     */
    private BigDecimal markingPrice;

    /**
     * 商品名称
     */
    private String commodityName;

    /**
     * 商品图片
     */
    private String commodityUrl;

    /**
     * 是否默认选中 0否 1是
     */
    private int defaultChoose;

    /**
     * 是否必选 0否 1是
     */
    private int requiredChoose;

    /**
     * 单品所属的商品ID
     */
    private Long spuId;

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

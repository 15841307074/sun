package com.htyoudao.youdao.module.commodity.dal.dataobject;

import com.baomidou.mybatisplus.annotation.*;
import com.htyoudao.youdao.framework.mybatis.core.dataobject.BaseDO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 商品对象 commodity_spus
 * 
 * @author Qizhongann
 * @date 2024-01-16
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("commodity_spus")
public class RecipeCommodityDO extends BaseDO {

    /**
     * 配方设置标识
     */
    private Integer skuFlag;

    /**
     * 商品规格是否改变
     */
    private Integer isChange;

    /**
     * 提示
     */
    private String tip = "商品规格有调整，为保证统计门店进销存数据准确度请及时优化配方";


    /**
     * 商品名称
     */
    private String commodityName;

    /**
     * 商品ID
     */
    private Long commodityId;

    /**
     * 图片
     */
    private String imageUrl;

    /**
     * 商品分类
     */
    private String categoryName;

    /**
     * 商品分类
     */
    private Long categoryId;

    /**
     * 是否为单品
     */
    private Integer isSingle;

    /**
     * 是否为隐藏
     */
    private Integer isHidden;

}

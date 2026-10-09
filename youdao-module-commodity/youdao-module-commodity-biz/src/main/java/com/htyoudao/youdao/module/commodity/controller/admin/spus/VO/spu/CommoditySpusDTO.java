package com.htyoudao.youdao.module.commodity.controller.admin.spus.VO.spu;

import com.baomidou.mybatisplus.annotation.TableField;

import com.htyoudao.youdao.module.commodity.dal.dataobject.TimeBase;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * PC做查询商品用
 */
@Data
public class CommoditySpusDTO extends TimeBase {
    /**
     * 分类 ID
     */
    private Long categoryId;
    /**
     * 商品 ID
     */
    private Long commodityId;
    /**
     * 批量规则 1上架 2下架 3删除 4移动分组 5改可售时间
     */
    private Integer sortingRules;

    /**
     * 商品名称
     */
    private String commodityName;

    private List<Long> commodityIds = new ArrayList<>();

    private String categoryName;

    private Integer sort;

    /**
     * 查询条件 1 全部 2 上架 3 下架
     */
    @TableField(exist = false)
    private Integer selectView;


    /** 小程序上下架状态 1 上架 0 下架*/
    private Integer wxStatus;

    /** 门店上下架状态 1 上架 0 下架*/
    private Integer storeStatus;
    /** 是否是单品 1是 2否*/
    private Integer isSingle;
    /** 1 wx 2dcj*/
    private Integer chooseView;

}

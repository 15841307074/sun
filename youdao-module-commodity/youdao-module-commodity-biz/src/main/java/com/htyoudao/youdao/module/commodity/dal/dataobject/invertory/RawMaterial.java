package com.htyoudao.youdao.module.commodity.dal.dataobject.invertory;

import com.htyoudao.youdao.framework.mybatis.core.dataobject.BusinessBaseDO;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 原材料基础信息表
 */
@Data
public class RawMaterial extends BusinessBaseDO {
    /**
     * 主键ID
     */
    private Long id;

    /**
     * 门店ID
     */
    private Long storeId;

    /**
     * 类别ID
     */
    private Long categoryId;

    /**
     * 原始商品ID
     */
    private Long commodityId;

    /**
     * 类别名称
     */
    private String categoryName;

    /**
     * 商品名称
     */
    private String commodityName;

    /**
     * 商品编号
     */
    private String commodityCode;

    /**
     * 商品统计名称
     */
    private String stasticsCommodityName;

    /**
     * 规格
     */
    private String specifications;

    /**
     * 规格单位换算规则
     */
    private String specificationsRules;

    /**
     * 品牌ID
     */
    private Long brandId;

    /**
     * 品牌名称
     */
    private String brandName;

    /**
     * 类型
     */
    private String commodityType;

    /**
     * 最小单位
     */
    private String minUnit;

    /**
     * 最小单位单价
     */
    private BigDecimal unitPrice;

    /**
     * 库存数量
     */
    private BigDecimal stock;

    /**
     * 版本号(乐观锁)
     */
    private Integer version;

    /**
     * 停用状态(0停用 1启用)
     */
    private Integer isEnable;

    /**
     * 出库单位
     */
    private String outUnit;

    /**
     * 出库单价
     */
    private BigDecimal outPrice;

    /**
     * 采购时间
     */
    private LocalDateTime inTime;

}
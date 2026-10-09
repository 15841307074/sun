package com.htyoudao.youdao.module.commodity.dal.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 小料对象 commodity_condiments
 *
 * @author Qizhongnan
 * @date 2024-01-16
 */
@Data
public class CommodityCondiments  {
    private static final long serialVersionUID = 1L;

    /**
     * 小料ID 弃用
     */
    @TableId(value = "condiment_id", type = IdType.ASSIGN_ID)
    private Long condimentId;

    /**
     * 小料名称
     */
    private String condimentName;

    /**
     * 价格
     */
    private BigDecimal price;

    /**
     * 数量
     */
    private Integer number;

    /**
     * 状态 1启用 0禁用
     */
    private Integer status;

    /**
     * 图片地址
     */
    private String imageUrl;
}

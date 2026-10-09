package com.htyoudao.youdao.module.commodity.api.DTO;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * <p>
 * 门店下商品的小料表
 * </p>
 *
 * @author qizhongnan
 * @since 2024-05-27
 */
@Data
public class CondimentInfoDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long condimentId;

    /**
     * 小料名称
     */
    private String condimentName;

    /**
     * 销量数量
     */
    private Integer number;

    /**
     * 小料价格
     */
    private BigDecimal price;

    /**
     * 小料图片
     */
    private String imageUrl;

    /**
     * 小料状态
     */
    private Integer status;
}

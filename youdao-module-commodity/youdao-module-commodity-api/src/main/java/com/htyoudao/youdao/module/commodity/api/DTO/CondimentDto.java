package com.htyoudao.youdao.module.commodity.api.DTO;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class CondimentDto implements Serializable {
    @Serial
    private static final long serialVersionUID = -3703470981651673369L;

    private Long condimentId;
    private String condimentName;
    private String imageUrl;
    private BigDecimal condimentPrice;
    private int status;
    /**
     * 数量
     */
    private Integer number;
    //顺序
    Integer sort;
}

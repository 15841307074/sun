package com.htyoudao.youdao.module.order.controller.app.order.DTO;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * 接收小程序购物车信息中的 BzCarGoods 信息
 * JavaJVM  2024/03/13
 */
@Data
public class SpBzCarGoodsDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 3857409898326240012L;
    /**
     * 小程序传过来的购物车中的商品信息
     */
    private List<SpBzCarListDTO> carList = new ArrayList<>();

    private BigDecimal price = BigDecimal.ZERO;

    private BigDecimal orderPrice = BigDecimal.ZERO;

    private BigDecimal payPrice = BigDecimal.ZERO;

    private Integer count = 0;

    private String storeId = "0";
}

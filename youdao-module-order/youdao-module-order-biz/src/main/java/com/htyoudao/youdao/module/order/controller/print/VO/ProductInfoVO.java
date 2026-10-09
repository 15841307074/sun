package com.htyoudao.youdao.module.order.controller.print.VO;

import lombok.Data;

/**
 * 该类表示商品信息，继承自 Information 类，并额外包含排序方式信息。
 */
@Data
public class ProductInfoVO extends InformationVO {
    /**
     * 商品信息的排序方式。 0 下单顺序  1 价格高-低  2 价格低-高
     */
    private Integer sortingMethod = 0;

}
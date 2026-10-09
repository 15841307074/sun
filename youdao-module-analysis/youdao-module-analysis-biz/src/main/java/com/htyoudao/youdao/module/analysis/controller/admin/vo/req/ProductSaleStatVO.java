package com.htyoudao.youdao.module.analysis.controller.admin.vo.req;

import lombok.Data;

@Data
public class ProductSaleStatVO {
    /** 商品ID */
    private Long commodityId;
    /** 商品名称 */
    private String goodsName;
    /** 总销量 */
    private Long totalSaleNum;
}

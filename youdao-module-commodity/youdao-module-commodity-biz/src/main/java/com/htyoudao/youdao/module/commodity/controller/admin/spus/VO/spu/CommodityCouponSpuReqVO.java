package com.htyoudao.youdao.module.commodity.controller.admin.spus.VO.spu;

import com.htyoudao.youdao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 优惠券 商品选择器
 */
@Data
public class CommodityCouponSpuReqVO extends PageParam {

    /**
     * 分类 ID
     */
    @Schema(description = "分类ID")
    private Long categoryId;
    /**
     * 商品名称
     */
    @Schema(description = "商品名称")
    private String commodityName;

    @Schema(description = "1单品 2套餐")
    private Integer isSingle;

    @Schema(description = "存在小料")
    private Boolean haveCondiments = false;

    @Schema(description = "存在属性")
    private Boolean haveFlavor = false;

}

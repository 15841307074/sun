package com.htyoudao.youdao.module.member.controller.admin.pointsProduct.VO;


import com.htyoudao.youdao.module.member.controller.app.pointsProduct.VO.CouponForProductsRespVo;
import com.htyoudao.youdao.module.member.dal.dataobject.coupon.CouponForProduct;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class PointsProductDTO {


    @Schema(description = "商品 id", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long productId;

    @Schema(description = "商品封面图")
    private String productThumbnail;

    @Schema(description = "商品封面类型，固定为2图片")
    private Integer productThumbnailType;


    @Schema(description = "商品名称", requiredMode = Schema.RequiredMode.REQUIRED)
    private String productName;


    @Schema(description = "商品价格", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long productPrice;


    @Schema(description = "商品状态 1.已售罄 2正常", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer productStatus;

    @Schema(description = "商品类型", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer productType;

    @Schema(description = "排序", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long productSort;


    @Schema(description = "库存", requiredMode = Schema.RequiredMode.REQUIRED)
    private int productInventory;

    @Schema(description = "优惠卷商品", requiredMode = Schema.RequiredMode.REQUIRED)
    CouponForProductsRespVo couponForProduct = new CouponForProductsRespVo();


}

package com.htyoudao.youdao.module.member.controller.admin.pointsProduct.VO;

import com.htyoudao.youdao.module.member.controller.app.pointsProduct.VO.CouponForProductsRespVo;
import com.htyoudao.youdao.module.member.controller.app.pointsProduct.VO.ImageDTO;
import com.htyoudao.youdao.module.member.dal.dataobject.coupon.CouponForProduct;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 积分商品详情
 */
@Data
public class PointsProductDetailDTO {
    /**
     * 是否领取过
     */
    @Schema(description = "是否领取过", requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean isReceive = false;
    /**
     * 商品 id
     */
    @Schema(description = "商品 id", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long productId;

    /**
     * 商品封面图
     */
    @Schema(description = "商品封面图")
    private String productThumbnail;

    /**
     * 商品名称
     */
    @Schema(description = "商品名称", requiredMode = Schema.RequiredMode.REQUIRED)
    private String productName;

    /**
     * 商品类型
     */
    @Schema(description = "商品类型", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer productType;

    /**
     * 商品价格
     */
    @Schema(description = "商品价格", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long productPrice;

    /**
     * 商品描述
     */
    @Schema(description = "商品描述", requiredMode = Schema.RequiredMode.REQUIRED)
    private String productDescription;

    /**
     * 商品状态 1.已售罄 2正常
     */
    @Schema(description = "商品状态 1.已售罄 2正常", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer productStatus;
    /**
     * 商品详情图
     */
    @Schema(description = "商品详情图", requiredMode = Schema.RequiredMode.REQUIRED)
    List<ImageDTO> productDetailImagelist;

    @Schema(description = "商品头图", requiredMode = Schema.RequiredMode.REQUIRED)
    List<ImageDTO> productHeaderImagelist;

    @Schema(description = "商品详情附件及对应类型")
    private List<PointsProductAttachmentVO> productDetailAttachments;

    @Schema(description = "商品头图附件及对应类型")
    private List<PointsProductAttachmentVO> productHeaderAttachments;


    @Schema(description = "优惠卷商品", requiredMode = Schema.RequiredMode.REQUIRED)
    CouponForProductsRespVo couponForProduct;

    @Schema(description = "商品类型", requiredMode = Schema.RequiredMode.REQUIRED)
    private long productInventory;


}

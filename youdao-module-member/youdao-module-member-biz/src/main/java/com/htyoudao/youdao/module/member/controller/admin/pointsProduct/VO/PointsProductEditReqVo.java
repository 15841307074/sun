package com.htyoudao.youdao.module.member.controller.admin.pointsProduct.VO;

import com.htyoudao.youdao.module.member.dal.dataobject.FlexBaseEntity;
import com.htyoudao.youdao.module.member.dal.dataobject.coupon.CouponForProduct;
import io.swagger.v3.oas.annotations.media.Schema;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Schema(description = "管理后台 - 积分商品修改参数 Request VO")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PointsProductEditReqVo extends FlexBaseEntity implements Serializable {


    @Schema(description = "商品 id", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "商品Id不能为空")
    private Long productId;


    @Schema(description = "商品封面图，供积分商城列表展示")
    private String productThumbnail;

    @Schema(description = "商品名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "商品名称不能为空")
    @Size(max = 20, message = "商品名称不能超过20个字")
    private String productName;


    @Schema(description = "商品排序", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "商品排序不能为空")
    private Long productSort;

    @Schema(description = "优惠券图片链接", requiredMode = Schema.RequiredMode.REQUIRED)
    String couponImageUrl;



    @Schema(description = "商品类型 1 优惠劵 2 实体积分商品", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "商品类型不能为空")
    private Integer productType;


    @Schema(description = "商品价格", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "商品价格不能为空")
    private Long productPrice;


    @Schema(description = "小程序上下架", requiredMode = Schema.RequiredMode.REQUIRED)
    @Min(value = 1, message = "小程序上下架状态只能为1或2")
    @Max(value = 2, message = "小程序上下架状态只能为1或2")
    private Integer isAvailable;


    @Schema(description = "商品描述", requiredMode = Schema.RequiredMode.REQUIRED)
    @Size(max = 200, message = "商品描述不能超过200个字")
    private String productDescription;


    @Schema(description = "商品头图", requiredMode = Schema.RequiredMode.REQUIRED)
    private String productHeaderImage;


    @Schema(description = "商品详情图", requiredMode = Schema.RequiredMode.REQUIRED)
    private String productDetailImages;


    @Schema(description = "优惠卷编号", requiredMode = Schema.RequiredMode.REQUIRED)
    private String couponCode;

    @Schema(description = "是否需要快递 1 是 2否", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer requiresShipping;

    @Schema(description = "商品状态 1.已售罄 2正常", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer productStatus;

    @Schema(description = "商品库存", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "商品库存不能为空")
    private Integer productInventory;

    @Schema(description = "商品分类ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long productCategoryId;

    @Schema(description = "优惠卷名称", requiredMode = Schema.RequiredMode.REQUIRED)
    private String couponName;

    @Schema(description = "优惠卷类型名称", requiredMode = Schema.RequiredMode.REQUIRED)
    private String couponTypeName;

    @Schema(description = "版本号", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long version;

    List<String> productHeaderImageList;

    List<String> productDetailImageList;

    @Schema(description = "商品封面图列表（兼容历史接参）")
    List<String> productThumbnailList;

    @Valid
    @Size(max = 5, message = "商品头图最多上传5个附件")
    @Schema(description = "商品头图附件，支持1视频和2图片，最多5个")
    private List<@NotNull(message = "商品头图附件不能为空") @Valid PointsProductAttachmentVO> productHeaderAttachments;

    @Valid
    @Size(max = 5, message = "商品详情最多上传5张图片")
    @Schema(description = "商品详情附件，仅支持2图片，最多5个")
    private List<@NotNull(message = "商品详情附件不能为空") @Valid PointsProductAttachmentVO> productDetailAttachments;

    CouponForProduct couponForProduct;

    /**
     * 商品详情附件只允许上传图片。
     */
    @JsonIgnore
    @AssertTrue(message = "商品详情附件只能为图片")
    public boolean isProductDetailAttachmentTypeValid() {
        return productDetailAttachments == null || productDetailAttachments.stream()
                .allMatch(attachment -> attachment != null && Integer.valueOf(2).equals(attachment.getType()));
    }

}

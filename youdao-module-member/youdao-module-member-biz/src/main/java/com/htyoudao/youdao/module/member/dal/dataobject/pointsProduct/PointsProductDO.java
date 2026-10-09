package com.htyoudao.youdao.module.member.dal.dataobject.pointsProduct;

import com.baomidou.mybatisplus.annotation.TableField;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.htyoudao.youdao.framework.mybatis.core.dataobject.BusinessBaseDO;
import com.htyoudao.youdao.module.member.dal.dataobject.coupon.CouponForProduct;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.htyoudao.youdao.module.member.controller.admin.pointsProduct.VO.PointsProductAttachmentVO;

/**
 * 积分商品对象 points_product
 *
 * @author Qizhongnan
 * @date 2024-02-02
 */
@EqualsAndHashCode(callSuper = true)
@TableName(value = "points_product")
@Data
public class PointsProductDO extends BusinessBaseDO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;


    @Schema(description = "商品 id", requiredMode = Schema.RequiredMode.REQUIRED)
    @TableId
    private Long productId;

    /**
     * 商品封面图
     */
    @Schema(description = "商品封面图")
    @TableField(value = "product_thumbnail")
    private String productThumbnail;

    @Schema(description = "商品封面图列表（兼容历史接参）")
    @TableField(exist = false)
    List<String> productThumbnailList;

    /**
     * 商品名称
     */
    @Schema(description = "商品名称", requiredMode = Schema.RequiredMode.REQUIRED)
    private String productName;

    /**
     * 商品排序
     */
    @Schema(description = "商品排序", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long productSort;
    //优惠券图片链接
    @Schema(description = "优惠券图片链接", requiredMode = Schema.RequiredMode.REQUIRED)
    String couponImageUrl;

    /**
     * 商品类型 1 优惠劵 2 实体积分商品
     */
    @Schema(description = "商品类型 1 优惠劵 2 实体积分商品", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer productType;

    /**
     * 商品价格
     */
    @Schema(description = "商品价格", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long productPrice;

    /**
     * 小程序上下架
     */
    @Schema(description = "小程序上下架", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer isAvailable;

    /**
     * 商品描述
     */
    @Schema(description = "商品描述", requiredMode = Schema.RequiredMode.REQUIRED)
    private String productDescription;

    /**
     * 商品头图
     */
    @Schema(description = "商品头图", requiredMode = Schema.RequiredMode.REQUIRED)
    private String productHeaderImage;

    /**
     * 商品头图附件 JSON，保存附件地址和图片/视频类型。
     */
    @JsonIgnore
    @TableField(value = "product_header_attachments")
    private String productHeaderAttachmentsJson;

    @TableField(exist = false)
    @Schema(description = "商品头图附件")
    private List<PointsProductAttachmentVO> productHeaderAttachments;


    @Schema(description = "商品头部列表", requiredMode = Schema.RequiredMode.REQUIRED)
    @TableField(exist = false)
    List<String> productHeaderImageList;

    /**
     * 商品详情图
     */
    @Schema(description = "商品详情图", requiredMode = Schema.RequiredMode.REQUIRED)
    private String productDetailImages;

    /**
     * 商品详情附件 JSON，详情附件只允许图片。
     */
    @JsonIgnore
    @TableField(value = "product_detail_attachments")
    private String productDetailAttachmentsJson;

    @TableField(exist = false)
    @Schema(description = "商品详情附件")
    private List<PointsProductAttachmentVO> productDetailAttachments;

    @Schema(description = "商品详情图片", requiredMode = Schema.RequiredMode.REQUIRED)
    @TableField(exist = false)
    List<String> productDetailImageList;


    /**
     * 优惠卷编号
     */
    @Schema(description = "优惠卷编号", requiredMode = Schema.RequiredMode.REQUIRED)
    private String couponCode;

    /**
     * 是否需要快递 1 是 2否
     */
    @Schema(description = "是否需要快递 1 是 2否", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer requiresShipping;

    /**
     * 商品状态 1.已售罄 2正常
     */
    @Schema(description = "商品状态 1.已售罄 2正常", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer productStatus;

    /**
     * 商品库存
     */
    @Schema(description = "商品库存", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer productInventory;

    /**
     * 商品分类ID
     */
    @Schema(description = "商品分类ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long productCategoryId;

    //优惠卷名称
    @Schema(description = "优惠卷名称", requiredMode = Schema.RequiredMode.REQUIRED)
    private String couponName;
    //优惠卷类型名称
    @Schema(description = "优惠卷类型名称", requiredMode = Schema.RequiredMode.REQUIRED)
    private String couponTypeName;

    @Schema(description = "优惠卷相关", requiredMode = Schema.RequiredMode.REQUIRED)
    @TableField(exist = false)
    CouponForProduct couponForProduct;

    @Schema(description = "版本", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long version;



}

package com.htyoudao.youdao.module.analysis.controller.admin.vo.req;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 商品销售数据写入DTO
 */
@Data
@Schema(description = "商品销售数据写入DTO")
public class ProductSaleDataDTO {

    @Schema(description = "订单商品ID")
    private String orderProductId;

    @Schema(description = "订单编号")
    private String orderSn;

    @Schema(description = "订单状态")
    private String orderState;

    @Schema(description = "门店ID")
    private String storeId;

    @Schema(description = "门店名称")
    private String storeName;

    @Schema(description = "会员ID")
    private String memberId;

    @Schema(description = "商品ID")
    private String goodsId;

    @Schema(description = "商品名称")
    private String goodsName;

    @Schema(description = "商品图片")
    private String goodsImage;

    @Schema(description = "规格值")
    private String specValues;

    @Schema(description = "商品展示价格")
    private BigDecimal goodsShowPrice;

    @Schema(description = "商品数量")
    private Integer goodsNum;

    @Schema(description = "活动优惠金额")
    private BigDecimal activityDiscountAmount;

    @Schema(description = "活动优惠明细")
    private String activityDiscountDetail;

    @Schema(description = "平台活动金额")
    private BigDecimal platformActivityAmount;

    @Schema(description = "平台券金额")
    private BigDecimal platformVoucherAmount;

    @Schema(description = "实付金额")
    private BigDecimal moneyAmount;

    @Schema(description = "佣金比例")
    private BigDecimal commissionRate;

    @Schema(description = "佣金金额")
    private BigDecimal commissionAmount;

    @Schema(description = "附加佣金金额")
    private BigDecimal attachCommissionAmount;

    @Schema(description = "拼团ID")
    private Integer spellTeamId;

    @Schema(description = "是否赠品")
    private String isGift;

    @Schema(description = "赠品ID")
    private Integer giftId;

    @Schema(description = "退货数量")
    private Integer returnNumber;

    @Schema(description = "是否评价")
    private String isComment;

    @Schema(description = "评价时间")
    private LocalDateTime commentTime;

    @Schema(description = "赠送积分")
    private Integer sendIntegral;

    @Schema(description = "是否单品")
    private Integer isSingle;

    @Schema(description = "口味名称")
    private String flavorName;

    @Schema(description = "口味值")
    private String flavorValue;

    @Schema(description = "分类ID")
    private String categoryId;

    @Schema(description = "分类名称")
    private String categoryName;

    @Schema(description = "品项ID")
    private String commodityId;

    @Schema(description = "项目归属")
    private String projectOwnerShip;

    @Schema(description = "业务ID")
    private String businessId;

    @Schema(description = "创建者")
    private String creator;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新者")
    private String updater;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

    @Schema(description = "是否删除")
    private String deleted;

    @Schema(description = "SKU划线价")
    private BigDecimal skuStrikePrice;

    @Schema(description = "活动ID")
    private String activityId;

    @Schema(description = "活动类型")
    private Integer activityType;

    @Schema(description = "活动名称")
    private String activityName;

    @Schema(description = "促销优惠金额")
    private BigDecimal promotionDiscountAmount;

    @Schema(description = "优惠券ID")
    private String couponId;

    @Schema(description = "用户优惠券ID")
    private String userCouponId;

    @Schema(description = "优惠券名称")
    private String couponName;

    @Schema(description = "原始SKU ID")
    private String originalSkuId;

    @Schema(description = "门店SKU ID")
    private String storeSkuId;
}

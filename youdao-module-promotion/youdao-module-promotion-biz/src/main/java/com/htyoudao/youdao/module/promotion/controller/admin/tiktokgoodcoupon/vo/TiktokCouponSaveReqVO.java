package com.htyoudao.youdao.module.promotion.controller.admin.tiktokgoodcoupon.vo;

import com.htyoudao.youdao.module.promotion.dal.dataobject.couponcommodity.CouponCommodityDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.couponstore.CouponStoreDO;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author dht
 */
@Schema(description = "管理后台 - 优惠券新增/修改 Request VO")
@Data
public class TiktokCouponSaveReqVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "15444")
    private Long id;

    @Schema(description = "优惠券类型(0-满减券,1-折扣券,2-兑换,3-抖音满减券,4-抖音兑换券)", example = "1")
    @NotNull(message = "优惠券类型(0-满减券,1-折扣券,2-兑换,3-抖音满减券,4-抖音兑换券)")
    private Integer couponType;

    @Schema(description = "优惠券名称", example = "0090")
    @NotEmpty(message = "优惠券名称不能为空")
    private String couponName;


    @Schema(description = "减免/折扣，兑换券的支付金额", example = "20557")
    //@NotNull(message = "减免/折扣不能为空")
    private BigDecimal reliefOrDiscount;

    @Schema(description = "优惠券图片", example = "https://www.iocoder.cn")
    @NotEmpty(message = "优惠券图片不能为空")
    private String couponImageUrl;


    @Schema(description = "发放总量")
    @NotNull(message = "发放总量不能为空")
    private Integer totalNum;

    @Schema(description = "优惠券说明")
    @NotEmpty(message = "优惠券说明不能为空")
    private String couponExplain;

    @Schema(description = "备注", example = "随便")
    private String remark;


    @Schema(description = "每人限领数目", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "每人限领数目不能为空")
    private Integer limitNum;


    @Schema(description = "优惠券适用商品")
    List<CouponCommodityDO> couponCommodities;

    @Schema(description = "优惠券适用门店")
    List<CouponStoreDO> couponStores;

    @Schema(description = "用餐方式 0 全部可用 1堂食可用 2外卖可用")
    @NotNull(message = "用餐方式不能为空")
    private Integer habit;


    @Schema(description = "适用门店范围 1:通用 2:门店券")
    @NotNull(message = "适用门店范围 1:通用 2:门店券不能为空")
    private Integer isCommon;


    @Schema(description = "适用商品范围 1 通用 2指定商品可用 3指定商品不可用", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "适用商品范围 1 通用 2指定商品可用 3指定商品不可用不能为空")
    private Integer isCommonStore;
}

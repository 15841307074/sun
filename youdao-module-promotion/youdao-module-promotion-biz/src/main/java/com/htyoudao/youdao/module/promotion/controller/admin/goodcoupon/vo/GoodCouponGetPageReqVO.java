package com.htyoudao.youdao.module.promotion.controller.admin.goodcoupon.vo;

import com.htyoudao.youdao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Schema(description = "管理后台 - 优惠券分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class GoodCouponGetPageReqVO extends PageParam {


    /**
     * 商品id
     */
    @Schema(description = "商品id")
    private Long commodityId;

    /**
     * 备注
     */
    @Schema(description = "备注")
    private String remark;

    /**
     * 门店id
     */
    @Schema(description = "门店id")
    private Long storeId;

    /**
     * 领取人限制 （0 不限制 ，1 新注册用户 ，2 老用户，3回归用户）
     */
    @Schema(description = "领取人限制 （0 不限制 ，1 新注册用户 ，2 老用户，3回归用户）")
    private Integer userRestrictions;

    /**
     * 优惠券类型(0-满减券,1-折扣券)
     */
    @Schema(description = "优惠券类型(0-满减券,1-折扣券)")
    private String couponType;

    /**
     * 是否上架(0-否,1-是)
     */
    @Schema(description = "是否上架(0-否,1-是)")
    private Integer isGround;

    /**
     * 门店所属组织
     */
    @Schema(description = "门店所属组织")
    private Long orgId;


    @Schema(description = "适用门店范围 1:通用 2:门店券")
    private Integer isCommon;
}

package com.htyoudao.youdao.module.promotion.controller.admin.advertising.carousel.VO;

import com.htyoudao.youdao.framework.common.pojo.PageParam;
import com.htyoudao.youdao.framework.common.validation.InEnum;
import com.htyoudao.youdao.module.promotion.enums.advertising.carousel.*;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Schema(description = "管理后台 - 轮播优惠券选择 Request VO")
@Data
public class CouponChooseReqVO extends PageParam {

    @Schema(description = "优惠券名称/备注")
    private String couponNameOrRemark;

    @Schema(description = "适用区域 1 全部可用 2 部分门店可用")
    @InEnum(CouponISCommonEnum.class)
    private Integer isCommon;

    @Schema(description = "适用商品范围 1 通用 2指定商品可用 3指定商品不可用")
    @InEnum(CouponISCommonStoreEnum.class)
    private Integer isCommonStore;

    @Schema(description = "优惠券类型 0 满减券 1 折扣券 2兑换券")
    @InEnum(CouponTypeEnum.class)
    private Integer couponType;

    @Schema(description = "领取限制 0 不限制 1 新注册用户 2 老用户 3 回归用户")
    @InEnum(UserRestrictionsEnum.class)
    private Integer userRestrictions;

    @Schema(description = "用餐方式 0 全部可用 1 堂食可用 2 外卖可用")
    @InEnum(HabitEnum.class)
    private Integer habit;

    @Schema(description = "商品ID")
    private Long commodityId;

}

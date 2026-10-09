package com.htyoudao.youdao.module.promotion.api.activity.VO;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Schema(description = "管理后台 - 营销集点活动创建 Request VO")
@Data
public class ActivityJDFullRespVO extends ActivityJDRespVO{

    /**
     * 可叠加活动（存储活动标识，如1-优惠券）
     */
    @Schema(description = "1 优惠卷")
    private List<ActivityJDCouponRespSaveVO> couponList;

    /**
     * 优惠券包
     */
    @Schema(description = "2 优惠包")
    private List<ActivityJDCouponPackageRespSaveVO> couponPackageList;

    @Schema(description = "门店ID集合")
    private List<StoreInfoRespVO> storeIds;

}

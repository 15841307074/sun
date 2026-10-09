package com.htyoudao.youdao.module.promotion.controller.admin.activityJD.vo;

import com.htyoudao.youdao.module.promotion.controller.admin.couponpackage.vo.CouponPackageRespVO;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Schema(description = "管理后台 - 营销活动秒杀新增/修改 Request VO")
@Data
public class ActivityJDCouponPackageCompareVO {

    @Schema(description = "奖品ID")
    @NotNull(message = "奖品名称 不能为空")
    private Long id;

    @Schema(description = "活动库存")
    @NotNull(message = "活动库存")
    private Integer inventory;

}

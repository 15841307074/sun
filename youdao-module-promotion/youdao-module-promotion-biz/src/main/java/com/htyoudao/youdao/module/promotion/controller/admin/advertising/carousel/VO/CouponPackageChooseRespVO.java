package com.htyoudao.youdao.module.promotion.controller.admin.advertising.carousel.VO;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - 轮播优惠券选择 Response VO")
@Data
public class CouponPackageChooseRespVO {

    @Schema(description = "优惠券ID")
    private String id;

    @Schema(description = "优惠券包名称")
    private String packageName;

    @Schema(description = "优惠券包备注")
    private String remark;

    @Schema(description = "领取限制 0 不限制 1 新注册用户 2 老用户 3 回归用户")
    private Integer userRestrictions;

    @Schema(description = "已领取数目")
    private Integer receivedNum;

    @Schema(description = "剩余数目")
    private Integer packageNum;

}

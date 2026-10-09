package com.htyoudao.youdao.module.promotion.controller.admin.couponpackage.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class CouponPackageCountRespVo {



    @Schema(description = "已领取")
    private Integer receivedNum;


//    @Schema(description = "已使用")
//    private Integer usedNum;
//
//    @Schema(description = "未使用")
//    private Integer notUsedNum;



}

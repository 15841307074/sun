package com.htyoudao.youdao.module.promotion.controller.admin.usercoupon.VO;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;

@Data
public class UserCouponCountRespVo {



    @Schema(description = "已领取")
    private Integer receivedNum;


    @Schema(description = "已使用")
    private Integer usedNum;

    @Schema(description = "未使用")
    private Integer notUsedNum;



}

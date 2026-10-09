package com.htyoudao.youdao.module.promotion.api.goodcoupon.DTO;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import static com.htyoudao.youdao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 优惠券分页 Request VO")
@Data
@ToString(callSuper = true)
public class GoodCouponDataDTO implements Serializable {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "15444")
    private Long id;

    @Schema(description = "优惠券名称")
    private String couponName;

    @Schema(description = "适用门店范围 1:通用 2:门店券")
    private Integer isCommon;


    @Schema(description = "门店id", example = "1024")
    private List<Long> storeIdList;

}

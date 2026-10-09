package com.htyoudao.youdao.module.promotion.controller.admin.couponpackage.vo;

import lombok.*;
import io.swagger.v3.oas.annotations.media.Schema;
import com.htyoudao.youdao.framework.common.pojo.PageParam;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;

import static com.htyoudao.youdao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

/**
 * @author dht
 */
@Schema(description = "管理后台 - 优惠券包分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class CouponPackagePageReqVO extends PageParam {

    @Schema(description = "优惠券包名", example = "赵六")
    private String packageName;


    @Schema(description = "是否上架(0-否,1-是)")
    private Integer isGround;



}
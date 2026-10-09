package com.htyoudao.youdao.module.promotion.controller.admin.analysis.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.htyoudao.youdao.framework.common.pojo.PageParam;
import com.htyoudao.youdao.framework.common.validation.InEnum;
import com.htyoudao.youdao.module.promotion.enums.advertising.carousel.CouponISCommonEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static com.htyoudao.youdao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(name = "优惠券分析请求参数", description = "优惠券分析请求参数")
@Data
public class CouponDataAnalysisReqVO extends PageParam {

    @Schema(description = "优惠券ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull
    private Long couponId;

    @Schema(description = "门店名称")
    private String storeName;

    @Schema(description = "组织ID")
    private Long orgId;

    @Schema(description = "1:通用 2:门店券", requiredMode = Schema.RequiredMode.REQUIRED)
    @InEnum(value = CouponISCommonEnum.class)
    @NotNull
    private Integer isCommon;

    @Schema(description = "开始时间")
    @JsonFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    @JsonSerialize(using = LocalDateTimeSerializer.class) // 序列化
    @JsonDeserialize(using = LocalDateTimeDeserializer.class) // 反序列化
    private LocalDateTime startTime;

    @Schema(description = "结束时间")
    @JsonFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    @JsonSerialize(using = LocalDateTimeSerializer.class) // 序列化
    @JsonDeserialize(using = LocalDateTimeDeserializer.class) // 反序列化
    private LocalDateTime endTime;

}

package com.htyoudao.youdao.module.promotion.controller.admin.activityJk.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.htyoudao.youdao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

import static com.htyoudao.youdao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ActivityJkExchangeExchangeReqVO {



    @Schema(name = "id",description = "活动id  必传")
    @NotNull
    private Long id;

    @Schema(name = "cardType", description = "卡片类型 1 兜底卡 2 套系卡 3 万能卡 4 隐藏卡")
    private Integer prizeType;

    @Schema(name = "memberName", description = "会员名称/手机号")
    private String memberName;

    @Schema(description = "开始时间")
    private String startTime;

    @Schema(description = "结束时间")
    private String endTime;

    @Schema(name = "receiveAddressType",description = "收货地址(0 未填  1 已填)")
    private Integer receiveAddressType;

    @Schema(name = "trackingType",description = "物流单号(0 未填  1 已填)")
    private Integer trackingType;

    @Schema(name = "claimStatus", description = "红包领取状态(1 未领取  2 已领取  3已过期)")
    private Integer claimStatus;

    @Schema(name = "storeId", description = "门店id")
    private Long storeId;


}

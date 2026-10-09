package com.htyoudao.youdao.module.promotion.controller.admin.activityexchangelog.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import io.swagger.v3.oas.annotations.media.Schema;
import com.htyoudao.youdao.framework.common.pojo.PageParam;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;
import java.util.Date;

import static com.htyoudao.youdao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

/**
 * @author dht
 */
@Schema(description = "管理后台 - 活动的兑换记录分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ExchangeLogPageReqVO extends PageParam {

    @Schema(description = "会员昵称/联系电话", example = "赵六")
    private String nameOrMobile;

    @Schema(description = "活动id", example = "28156")
    @NotNull(message = "活动id不能为空")
    private Long activityId;

    @Schema(description = "奖品类型 0 优惠券 1券包", example = "1")
    private Integer awardType;

    @Schema(description = "开始时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date lotteryStartDate;

    @Schema(description = "结束时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date lotteryEndDate;
}
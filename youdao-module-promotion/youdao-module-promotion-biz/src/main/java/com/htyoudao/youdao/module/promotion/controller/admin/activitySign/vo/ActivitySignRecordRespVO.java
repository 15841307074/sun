package com.htyoudao.youdao.module.promotion.controller.admin.activitySign.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - 签到记录 Response VO")
@Data
public class ActivitySignRecordRespVO {

    @Schema(description = "签到流水ID")
    private Long id;

    @Schema(description = "活动ID")
    private Long activityId;

    @Schema(description = "手机号")
    private String memberMobile;

    @Schema(description = "本次触发签到的会员ID")
    private Long memberId;

    @Schema(description = "会员昵称")
    private String memberName;

    @Schema(description = "来源门店ID")
    private Long storeId;

    @Schema(description = "来源门店名称")
    private String storeName;

    @Schema(description = "签到日期")
    private LocalDate signDate;

    @Schema(description = "签到时间")
    private LocalDateTime signTime;

    @Schema(description = "签到类型：1正常签到 2补签 3后台补录")
    private Integer signType;

    @Schema(description = "连签周期次数：根据查询入参continuousCycleDays汇总，计算该用户满足连签周期的次数")
    private Integer continuousCycleCount;

    @Schema(description = "连续签到天数：查询时间段内，该用户连续签到最多天数")
    private Integer maxContinuousDays;

    @Schema(description = "累计签到天数：查询时间段内，该用户累计签到天数之和")
    private Integer totalSignDays;
}

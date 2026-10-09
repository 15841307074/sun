package com.htyoudao.youdao.module.promotion.controller.admin.activitySign.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.htyoudao.youdao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - 签到记录分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
public class ActivitySignRecordPageReqVO extends PageParam {

    @Schema(description = "活动ID，当前活动详情页必传", requiredMode = Schema.RequiredMode.REQUIRED, example = "10086")
    @NotNull(message = "活动ID不能为空")
    private Long activityId;

    @Schema(description = "手机号", example = "13800138000")
    private String memberMobile;

    @Schema(description = "签到门店ID", example = "101")
    private Long storeId;

    @Schema(description = "连签周期；必填，前端默认从活动详情continuousCycleDays取值；允许用户输入>0整数，最多365；用于汇总计算用户满足连签周期的次数", requiredMode = Schema.RequiredMode.REQUIRED, example = "7")
    @NotNull(message = "连签周期不能为空")
    @Min(value = 1, message = "连签周期最小为1")
    @Max(value = 365, message = "连签周期最大为365")
    private Integer continuousCycleDays;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @Schema(description = "签到开始时间", example = "2026-06-01 00:00:00")
    private LocalDateTime signTimeStart;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @Schema(description = "签到结束时间", example = "2026-06-30 23:59:59")
    private LocalDateTime signTimeEnd;
    @Schema(hidden = true, description = "后端调试参数：true 时跳过中间件，直接查询 MySQL")
    private Boolean forceMysql;

}

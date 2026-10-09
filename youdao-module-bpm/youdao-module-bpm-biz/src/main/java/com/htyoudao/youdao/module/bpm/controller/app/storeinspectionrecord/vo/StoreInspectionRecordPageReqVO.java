package com.htyoudao.youdao.module.bpm.controller.app.storeinspectionrecord.vo;

import com.alibaba.excel.annotation.ExcelProperty;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.util.*;
import io.swagger.v3.oas.annotations.media.Schema;
import com.htyoudao.youdao.framework.common.pojo.PageParam;
import java.math.BigDecimal;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;

import static com.htyoudao.youdao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "app - 巡店记录主分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class StoreInspectionRecordPageReqVO extends PageParam {

    @Schema(description = "门店name", example = "28721")
    private String storeName;

    private List<Long> storeIds;

    @Schema(description = "使用的模板ID", example = "3751")
    private Long templateId;

    @Schema(description = "状态：0 进行中, 1 已完成, 2 已失效", example = "1")
    private Integer status;

    @Schema(description = "巡店人IDS", example = "13195")
    private List<Long> inspectorIds;

    @Schema(description = "奖惩金额", example = "30088")
    private BigDecimal rewardAmount;

    @Schema(description = "开始时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    @JsonFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @NotNull(message = "time不能为空")
    private LocalDateTime startTime;

    @Schema(description = "完成时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    @JsonFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @NotNull(message = "time不能为空")
    private LocalDateTime endTime;

    @Schema(description = "巡店定位", example = "0090")
    private String storePatrolPosition;

    @Schema(description = "整改意见", example = "0090")
    private String rectificationOpinion;
//
//    @Schema(description = "模板预设满分")
//    private Integer totalScore;
//
//    @Schema(description = "实际得分")
//    private Integer actualScore;
//
//    @Schema(description = "得分率")
//    private BigDecimal scoreRate;
//
//    @Schema(description = "点检项目总数", example = "4805")
//    private Integer itemCount;
//
//    @Schema(description = "合格数", example = "1916")
//    private Integer qualifiedCount;
//
//    @Schema(description = "不合格数", example = "30088")
//    private Integer unqualifiedCount;
//
//    @Schema(description = "不适用数", example = "8214")
//    private Integer notApplicableCount;
//
//    @Schema(description = "创建时间")
//    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
//    @JsonFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND, timezone = "GMT+8")
//    private LocalDateTime createTime;

}
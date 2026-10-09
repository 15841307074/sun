package com.htyoudao.youdao.module.bpm.controller.app.storeinspectionrecordreport.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.htyoudao.youdao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static com.htyoudao.youdao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "app - 巡店概览 Request VO")
@Data
public class InspectionReportReqVO extends PageParam {

    @Schema(description = "巡店人ID", example = "100")
    private Long inspectorId;

    @Schema(description = "门店ID", example = "100")
    private Long storeId;

    @Schema(description = "使用的模板ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "3751")
    private Long templateId;

    @Schema(description = "开始时间")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @NotNull(message = "time不能为空")
    private LocalDateTime beginTime;

    @Schema(description = "结束时间")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @NotNull(message = "time不能为空")
    private LocalDateTime endTime;

    @Schema(description = "排序字段", requiredMode = Schema.RequiredMode.NOT_REQUIRED, example = "3751")
    @NotNull(message = "排序字段不能为空")
    private String sortBy;

    @Schema(description = "0升序 1降序", requiredMode = Schema.RequiredMode.NOT_REQUIRED, example = "3751")
    @NotNull(message = "sortOrder不能为空")
    private Integer sortOrder;
}

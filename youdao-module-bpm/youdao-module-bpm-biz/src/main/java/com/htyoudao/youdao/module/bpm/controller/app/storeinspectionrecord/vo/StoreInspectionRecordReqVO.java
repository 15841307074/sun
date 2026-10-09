package com.htyoudao.youdao.module.bpm.controller.app.storeinspectionrecord.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Schema(description = "app - 巡店记录主分页 Request VO")
@Data
public class StoreInspectionRecordReqVO {

    @Schema(description = "门店ID", example = "12345")
    private Long storeId;

    @Schema(description = "巡店人ID", example = "67890")
    private Long inspectorId;

    @Schema(description = "使用的模板ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "3751")
    private Long templateId;

    @Schema(description = "巡店状态 0-待巡店, 1-巡店中, 2-已完成", example = "0-待巡店, 1-巡店中, 2-已完成")
    private Integer status;

    @Schema(description = "开始时间")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @NotNull(message = "time不能为空")
    private LocalDateTime startTime;

    @Schema(description = "结束时间")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @NotNull(message = "time不能为空")
    private LocalDateTime endTime;

    @Schema(description = "查看范围 0 全部 1我的", example = "0")
    @NotNull(message = "viewingRange不能为空")
    private Integer viewingRange;
}

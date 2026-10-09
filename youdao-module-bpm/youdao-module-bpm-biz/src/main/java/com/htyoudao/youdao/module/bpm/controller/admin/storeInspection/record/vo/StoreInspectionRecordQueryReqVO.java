package com.htyoudao.youdao.module.bpm.controller.admin.storeInspection.record.vo;

import com.alibaba.excel.annotation.ExcelProperty;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.htyoudao.youdao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;

import static com.htyoudao.youdao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 巡店记录 Request VO")
@Data
public class StoreInspectionRecordQueryReqVO extends PageParam {

    @Schema(description = "门店ID")
    private Long storeId;

    @Schema(description = "组织ID")
    private Long orgId;

    @Schema(description = "巡店人")
    private Long inspectorId;

    @Schema(description = "巡店模板")
    private Long templateId;

    @Schema(description = "开始时间")
    private String startTime;

    @Schema(description = "完成时间")
    private String endTime;

    @Schema(description = "巡店状态")
    private Integer status;

    @Schema(description = "排序字段")
    private String sortBy;

    @Schema(description = "排序")
    private String sortOrder;

    @Schema(description = "开始时间")
    private LocalDateTime startTimeDate; // 改成 String

    @Schema(description = "完成时间")
    private LocalDateTime endTimeDate;   // 改成 String



}

package com.htyoudao.youdao.module.bpm.controller.app.storeinspectionrecord.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.htyoudao.youdao.module.bpm.controller.app.storeinspectionrecorditem.vo.StoreInspectionRecordItemRespVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.math.BigDecimal;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;
import java.util.List;

import com.alibaba.excel.annotation.*;

import static com.htyoudao.youdao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "app - 当前人巡店记录主 Response VO")
@Data
@ExcelIgnoreUnannotated
public class IndividualInspectionRecordRespVO {

    @Schema(description = "巡店记录ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1098")
    @ExcelProperty("巡店记录ID")
    private Long id;

    @Schema(description = "项目ID", example = "23255")
    @ExcelProperty("项目ID")
    private Long businessId;

    @Schema(description = "门店ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "28721")
    @ExcelProperty("门店ID")
    private Long storeId;

    @Schema(description = "门店名称(快照)", example = "李四")
    @ExcelProperty("门店名称(快照)")
    private String storeName;

    @Schema(description = "使用的模板ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "3751")
    @ExcelProperty("使用的模板ID")
    private Long templateId;

    @Schema(description = "模板名称(快照)", example = "王五")
    @ExcelProperty("模板名称(快照)")
    private String templateName;

    @Schema(description = "状态：0 进行中, 1 已完成, 2 已失效", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @ExcelProperty("状态：0 进行中, 1 已完成, 2 已失效")
    private Integer status;

    @Schema(description = "巡店人ID", example = "13195")
    @ExcelProperty("巡店人ID")
    private Long inspectorId;

    @Schema(description = "巡店人姓名", example = "0090")
    @ExcelProperty("巡店人姓名")
    private String inspectorName;

    @Schema(description = "是否在范围内 0在 1不在")
    @ExcelProperty("是否在范围内 0在 1不在")
    private Boolean inScope;

    @Schema(description = "开始时间")
    @ExcelProperty("开始时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime startTime;

    @Schema(description = "完成时间")
    @ExcelProperty("完成时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime endTime;

    @Schema(description = "实际结束时间")
    @ExcelProperty("实际结束时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime overTime;

    @Schema(description = "模板预设满分")
    @ExcelProperty("模板预设满分")
    private Integer totalScore;

    @Schema(description = "实际得分")
    @ExcelProperty("实际得分")
    private Integer actualScore;

    @Schema(description = "得分率")
    @ExcelProperty("得分率")
    private BigDecimal scoreRate;

    @Schema(description = "点检项目总数", example = "4805")
    @ExcelProperty("点检项目总数")
    private Integer itemCount;

    @Schema(description = "合格数", example = "1916")
    @ExcelProperty("合格数")
    private Integer qualifiedCount;

    @Schema(description = "不合格数", example = "30088")
    @ExcelProperty("不合格数")
    private Integer unqualifiedCount;

    @Schema(description = "不适用数", example = "8214")
    @ExcelProperty("不适用数")
    private Integer notApplicableCount;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime createTime;

    @Schema(description = "奖惩金额", example = "30088")
    private BigDecimal rewardAmount;

    @Schema(description = "巡店定位", example = "0090")
    private String storePatrolPosition;

    @Schema(description = "分类统计")
    private InspectionTypeStatRespVO inspectionTypeStat;

    @Schema(description = "巡检详情", example = "")
    private List<StoreInspectionRecordItemRespVO> detail;

}
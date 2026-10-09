package com.htyoudao.youdao.module.bpm.controller.app.storeinspectionrecord.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;

@Schema(description = "app - 巡店记录主新增/修改 Request VO")
@Data
public class StoreInspectionRecordSaveReqVO {

    private Long id;

    @Schema(description = "门店ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "28721")
    @NotNull(message = "门店ID不能为空")
    private Long storeId;

    @Schema(description = "门店名称(快照)", example = "李四")
    @NotNull(message = "门店名称不能为空")
    private String storeName;

    @Schema(description = "使用的模板ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "3751")
    @NotNull(message = "使用的模板ID不能为空")
    private Long templateId;

    @Schema(description = "模板名称(快照)", example = "王五")
    @NotNull(message = "模板名称(快照)没传")
    private String templateName;

    @Schema(description = "巡店人ID", example = "13195")
    @NotNull(message = "inspectorId没传")
    private Long inspectorId;

    @Schema(description = "巡店人姓名", example = "0090")
    @NotNull(message = "inspectorName没传")
    private String inspectorName;

    @Schema(description = "巡店人电话", example = "0090")
    @NotNull(message = "inspectorPhone没传")
    private String inspectorPhone;

    @Schema(description = "门店照片", example = "0090")
    @NotNull(message = "门店照片没传")
    private String storePic;

    @Schema(description = "巡店定位", example = "0090")
    @NotNull(message = "storePatrolPositioning没传")
    private String storePatrolPosition;

    @Schema(description = "整改意见", example = "0090")
    private String rectificationOpinion;


//
//    @Schema(description = "是否在范围内 0在 1不在")
//    private Integer inScope;
//
//    @Schema(description = "开始时间")
//    private LocalDateTime startTime;
//
//    @Schema(description = "完成时间")
//    private LocalDateTime endTime;
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

}
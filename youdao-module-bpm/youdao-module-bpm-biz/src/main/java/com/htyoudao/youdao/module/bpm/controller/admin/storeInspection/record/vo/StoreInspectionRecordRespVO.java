package com.htyoudao.youdao.module.bpm.controller.admin.storeInspection.record.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Schema(description = "管理后台 - 巡店记录 Response VO")
@Data
public class StoreInspectionRecordRespVO {

    @Schema(description = "寻店记录ID")
    private Long id;

    @Schema(description = "巡店人ID")
    private Long inspectorId;

    @Schema(description = "巡店人姓名")
    private String inspectorName;

    @Schema(description = "巡店模板")
    private Long templateName;

    @Schema(description = "点检数")
    private Integer checklistCount;

    @Schema(description = "合格数")
    private Integer qualifiedCount;

    @Schema(description = "不合格数")
    private Integer unqualifiedCount;

    @Schema(description = "不适用数")
    private Integer notApplicableCount;

    @Schema(description = "得分")
    private Integer actualScore;

    @Schema(description = "总得分")
    private Integer totalScore;

    @Schema(description = "总得率")
    private BigDecimal scoreRate;

    @Schema(description = "奖惩金额")
    private Double rewardAmount;

    @Schema(description = "巡店状态")
    private Integer status;
}

package com.htyoudao.youdao.module.bpm.controller.app.storeinspectionrecord.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.PipedReader;
import java.math.BigDecimal;
import java.util.List;

@Schema(description = "app - 门店巡店记录主 Response VO")
@Data
public class StoreInspectionRecordRespVO {

    @Schema(description = "组织id", example = "1")
    private Long orgId;

    @Schema(description = "组织name", example = "纽约市")
    private String orgName;

    @Schema(description = "组织负责人id", example = "16")
    private String orgLeaderId;

    @Schema(description = "组织负责人name", example = "路易十六")
    private String orgLeaderName;

    @Schema(description = "组织负责人电话", example = "1547")
    private String orgLeaderPhone;

    @Schema(description = "门店ID", example = "10001")
    private Long storeId;

    @Schema(description = "门店名称", example = "沈阳航空航天大学店")
    private String storeName;

    @Schema(description = "店长ID", example = "10001")
    private Long storeLeaderId;

    @Schema(description = "店长name", example = "10001")
    private String storeLeaderName;

    @Schema(description = "奖惩金额", example = "30088")
    private BigDecimal rewardAmount;

    @Schema(description = "巡店定位", example = "0090")
    private String storePatrolPosition;

    @Schema(description = "巡店记录", example = "10001")
    private List<IndividualInspectionRecordRespVO> individualInspectionRecordList;
}

package com.htyoudao.youdao.module.promotion.controller.admin.activityNjnz.vo;

import com.alibaba.fastjson2.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.htyoudao.youdao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import java.time.LocalDate;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Schema(description = "管理后台 - 活动查询  VO")
@Data
public class ActivityPageReqVO extends PageParam {

    @Schema(description = "活动名称")
    private String activityName;

    @Schema(description = "活动类型 1（n件n折）")
    private Integer activityType;

    @Schema(description = "是否上架(0不开启 1开启)")
    private List<Integer> isEnabled;

    @Schema(description = "活动条件开始日期", requiredMode = RequiredMode.REQUIRED)
    @JsonFormat(pattern = "yyyy-MM-dd",timezone = "GMT+8")
    @JSONField(format="yyyy-MM-dd")
    private LocalDate startTime;

    @Schema(description = "活动条件结束日期", requiredMode = RequiredMode.REQUIRED)
    @JsonFormat(pattern = "yyyy-MM-dd",timezone = "GMT+8")
    @JSONField(format="yyyy-MM-dd")
    private LocalDate endTime;

    @Schema(description = "活动状态(1 未开始 2 进行中 3 已结束)")
    private Integer activityStatus;

    @Schema(description = "适用门店")
    private Long storeId;

    @Schema(description = "适用组织")
    private Long orgId;

}

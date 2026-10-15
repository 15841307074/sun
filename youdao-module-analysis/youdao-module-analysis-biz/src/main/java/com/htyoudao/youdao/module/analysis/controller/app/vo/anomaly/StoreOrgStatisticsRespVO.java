package com.htyoudao.youdao.module.analysis.controller.app.vo.anomaly;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Schema(description = "App - 月度门店组织统计")
@Data
public class StoreOrgStatisticsRespVO {

    @Schema(description = "月份", example = "2026-08")
    private String month;

    @Schema(description = "所选组织ID，字符串用于避免前端精度丢失")
    private String orgId;

    @Schema(description = "所选组织名称")
    private String orgName;

    @Schema(description = "所选组织及全部下级的去重门店数")
    private Long totalStoreCount;

    @Schema(description = "直属子组织与省份的平铺统计")
    private List<StoreOrgStatisticsItemRespVO> items;
}

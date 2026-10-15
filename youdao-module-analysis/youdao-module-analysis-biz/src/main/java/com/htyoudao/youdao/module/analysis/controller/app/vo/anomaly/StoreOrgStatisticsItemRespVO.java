package com.htyoudao.youdao.module.analysis.controller.app.vo.anomaly;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "App - 月度门店组织统计明细")
@Data
public class StoreOrgStatisticsItemRespVO {

    @Schema(description = "所选组织的直属子组织ID；直属门店使用所选组织ID")
    private String groupOrgId;

    @Schema(description = "分组组织名称")
    private String groupOrgName;

    @Schema(description = "省份；门店未设置省份时返回‘未设置’")
    private String provinceName;

    @Schema(description = "门店数")
    private Long storeCount;
}

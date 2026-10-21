package com.htyoudao.youdao.module.analysis.controller.app.vo.anomaly;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Schema(description = "App - 实时门店组织统计")
@Data
public class StoreOrgStatisticsRespVO {

    @Schema(description = "所选组织ID，字符串用于避免前端精度丢失")
    private String orgId;

    @Schema(description = "所选组织名称")
    private String orgName;

    @Schema(description = "所选组织及全部下级的去重门店数")
    private Long totalStoreCount;

    @Schema(description = "直接归属于所选组织的门店数")
    private Long directStoreCount;

    @Schema(description = "所选组织的直属子组织树，按组织层级动态返回")
    private List<StoreOrgStatisticsNodeRespVO> items;
}

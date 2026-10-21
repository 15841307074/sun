package com.htyoudao.youdao.module.analysis.controller.app.vo.anomaly;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Schema(description = "App - 实时门店组织统计节点")
@Data
public class StoreOrgStatisticsNodeRespVO {

    @Schema(description = "组织ID，字符串用于避免前端精度丢失")
    private String orgId;

    @Schema(description = "组织名称")
    private String orgName;

    @Schema(description = "组织层级")
    private Integer level;

    @Schema(description = "直接归属于当前组织的门店数")
    private Long directStoreCount;

    @Schema(description = "当前组织及全部下级的门店总数")
    private Long storeCount;

    @Schema(description = "直属子组织；没有下级时返回空数组")
    private List<StoreOrgStatisticsNodeRespVO> children;
}

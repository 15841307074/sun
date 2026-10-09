package com.htyoudao.youdao.module.system.controller.app.store.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "app - 门店基础信息 Response VO")
public class StoreBasicInfoRespVO {

    @Schema(description = "组织id", example = "1")
    private Long orgId;

    @Schema(description = "组织name", example = "纽约市")
    private String orgName;

    @Schema(description = "组织负责人id", example = "16")
    private Long orgLeaderId;

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

    @Schema(description = "店长phone", example = "10001")
    private String storeLeaderPhone;

    @Schema(description = "店长name", example = "10001")
    private String storeLeaderName;


}

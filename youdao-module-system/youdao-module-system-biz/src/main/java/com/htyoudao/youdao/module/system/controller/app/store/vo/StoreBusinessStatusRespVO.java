package com.htyoudao.youdao.module.system.controller.app.store.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Schema(description = "门店营业及外卖状态 Response VO")
@Data
public class StoreBusinessStatusRespVO {

    @Schema(description = "门店ID", example = "1")
    private Long storeId;

    @Schema(description = "当前门店是否营业")
    private Boolean storeOpen;

    @Schema(description = "门店营业时间段")
    private List<String> storeHoursList;

    @Schema(description = "当前门店外卖是否开启")
    private Boolean takeawayOpen;

    @Schema(description = "门店外卖时间段")
    private List<String> deliveryTimeList;

    @Schema(description = "当前门店代取订单是否开启")
    private Boolean errandOpen;

    @Schema(description = "门店代取订单开关：0开启，1关闭")
    private Integer campusDeliveryStatus;
}

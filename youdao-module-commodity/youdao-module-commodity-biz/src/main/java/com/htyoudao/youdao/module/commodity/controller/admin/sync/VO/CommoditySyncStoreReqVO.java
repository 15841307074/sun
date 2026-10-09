package com.htyoudao.youdao.module.commodity.controller.admin.sync.VO;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CommoditySyncStoreReqVO {
    @Schema(description = "门店 id")
    private Long storeId;

    @Schema(description = "门店名称")
    private String storeName;

    @Schema(description = "状态 0-队列中 1-执行中 2-已完成 3-失败")
    private Integer status;

    @Schema(description = "失败信息")
    private String errorMsg;
}

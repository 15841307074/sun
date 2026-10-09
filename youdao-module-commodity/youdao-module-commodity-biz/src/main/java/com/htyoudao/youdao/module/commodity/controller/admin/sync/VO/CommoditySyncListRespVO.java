package com.htyoudao.youdao.module.commodity.controller.admin.sync.VO;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CommoditySyncListRespVO {

    @Schema(description = "同步开始时间")
    private LocalDateTime startTime;

    @Schema(description = "同步结束时间")
    private LocalDateTime endTime;

    @Schema(description = "同步内容(模板/商品)")
    private String params;

    @Schema(description = "门店数量")
    private Long storeCount;

    @Schema(description = "同步状态")
    private String status;

    @Schema(description = "批次号")
    private String batchNo;

    @Schema(description = "同步人ID")
    private String syncUserId;

    @Schema(description = "同步人")
    private String syncUserName;
}

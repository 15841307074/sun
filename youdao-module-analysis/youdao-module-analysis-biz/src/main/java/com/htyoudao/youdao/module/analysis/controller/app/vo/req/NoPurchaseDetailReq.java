package com.htyoudao.youdao.module.analysis.controller.app.vo.req;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class NoPurchaseDetailReq extends PurchaseStatsReq {

    @NotNull
    private Long warehouseId;
}

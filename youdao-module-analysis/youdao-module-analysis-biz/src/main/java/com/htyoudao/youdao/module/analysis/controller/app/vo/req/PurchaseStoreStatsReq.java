package com.htyoudao.youdao.module.analysis.controller.app.vo.req;


import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class PurchaseStoreStatsReq extends PurchaseStatsReq {

    @Min(1)
    private int pageNo = 1;

    @Min(1)
    private int pageSize = 50;
}


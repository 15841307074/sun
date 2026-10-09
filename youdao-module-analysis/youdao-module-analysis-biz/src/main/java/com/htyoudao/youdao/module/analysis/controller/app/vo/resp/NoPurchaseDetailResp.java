package com.htyoudao.youdao.module.analysis.controller.app.vo.resp;

import lombok.Data;

import java.util.List;


@Data
public class NoPurchaseDetailResp {
    private Long warehouseId;
    private String startDate;
    private String endDate;
    private List<NoPurchaseDetailRow> rows;
}

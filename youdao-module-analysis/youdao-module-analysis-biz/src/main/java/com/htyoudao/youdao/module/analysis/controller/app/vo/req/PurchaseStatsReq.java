package com.htyoudao.youdao.module.analysis.controller.app.vo.req;

import com.htyoudao.youdao.module.analysis.enums.DateRangeMode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class PurchaseStatsReq {

    @NotNull
    private DateRangeMode mode; // WEEK/MONTH/CUSTOM

    @NotBlank
    private String startDate; // yyyy-MM-dd

    @NotBlank
    private String endDate;   // yyyy-MM-dd

    /**
     * 特殊说明：所有接口入参都必须带门店ID集合（作为所有查询条件）
     */
    private List<Long> storeIds;
}

package com.htyoudao.youdao.module.analysis.controller.admin.vo.res.member;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class AnalysisExpressVO {
    @Schema(description = "新客数量")
    private Long newCustomerCount;

    @Schema(description = "老客数量")
    private Long oldCustomerCount;
}

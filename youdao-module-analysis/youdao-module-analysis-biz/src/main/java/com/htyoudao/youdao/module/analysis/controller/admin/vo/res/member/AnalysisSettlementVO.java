package com.htyoudao.youdao.module.analysis.controller.admin.vo.res.member;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class AnalysisSettlementVO {
    @Schema(description = "新客数量")
    private Long memberCustomerCount;

    @Schema(description = "老客数量")
    private Long notMemberCustomerCount;
}

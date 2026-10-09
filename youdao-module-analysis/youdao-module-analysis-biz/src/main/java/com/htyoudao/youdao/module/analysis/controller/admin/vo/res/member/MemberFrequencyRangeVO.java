package com.htyoudao.youdao.module.analysis.controller.admin.vo.res.member;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class MemberFrequencyRangeVO {

    @Schema(description = "1单")
    private Double range1to2 = 0.0;

    @Schema(description = "2-3单")
    private Double range2to4 = 0.0;

    @Schema(description = ">3单")
    private Double range4 = 0.0;

}

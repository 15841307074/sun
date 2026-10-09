package com.htyoudao.youdao.module.analysis.controller.admin.vo.res.member;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class MemberDelayDaysRangeVO {

    @Schema(description = "0-10天")
    private Double range0to10;

    @Schema(description = "11-20")
    private Double range11to20;

    @Schema(description = "21-30")
    private Double range21to30;
}

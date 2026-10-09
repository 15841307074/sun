package com.htyoudao.youdao.module.promotion.controller.admin.activityJk.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ActivityJkStatisticsRespVO {




    @Schema(name = "参与人数", description = "参与人数")
    private Long participation;


    @Schema(name = "参与次数", description = "参与次数")
    private Long count;




}

package com.htyoudao.youdao.module.bpm.controller.app.storeinspectionrecord.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.math.BigDecimal;

@Schema(description = "app - 巡店分类统计 Response VO")
@Data
public class InspectionTypeStatRespVO {

    @Schema(description = "分类名称", example = "环境卫生")
    private String typeName;

    @Schema(description = "已点检数 (已检查项数)", example = "8")
    private Integer checkedCount;

    @Schema(description = "分类总点检数 (该类下总项数)", example = "10")
    private Integer totalCount;

    @Schema(description = "分类得分", example = "18")
    private Integer actualScore;

    @Schema(description = "分类总分 (满分)", example = "20")
    private Integer totalScore;

    @Schema(description = "分类得分率 (无%)", example = "90.00")
    private BigDecimal scoreRate;

}
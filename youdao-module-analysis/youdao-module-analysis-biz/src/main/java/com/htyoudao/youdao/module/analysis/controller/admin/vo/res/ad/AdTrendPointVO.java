package com.htyoudao.youdao.module.analysis.controller.admin.vo.res.ad;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Schema(description = "广告每日趋势数据")
@NoArgsConstructor
public class AdTrendPointVO {

    /**
     * 日期（yyyy-MM-dd）
     */
    @Schema(description = "日期", example = "2024-01-01")
    private String date;

    /**
     * 当日广告曝光PV
     */
    @Schema(description = "当日广告曝光PV", example = "100")
    private Long pv;

    /**
     * 当日广告曝光UV（基于曝光事件去重）
     */
    @Schema(description = "当日广告曝光UV（基于曝光事件去重）", example = "30")
    private Long uv;

    /**
     * 当日广告点击PV
     */
    @Schema(description = "当日广告点击PV", example = "10")
    private Long clicks;

    /**
     * 当日点击率 CTR（口径：点击量/曝光量 * 100%，保留两位小数；曝光为0时返回0）
     */
    @Schema(description = "当日点击率CTR（点击量/曝光量 * 100%，保留两位小数）", example = "10.00")
    private Double ctr;

}

package com.htyoudao.youdao.module.analysis.controller.admin.vo.res.ad;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Schema(description = "广告按位置分组统计数据")
@NoArgsConstructor
public class AdPositionStatVO {

    /**
     * 广告位序号（1-12）
     */
    @Schema(description = "广告位序号（1-12）", example = "1")
    private Integer adInfoPosition;

    /**
     * 该广告位曝光PV
     */
    @Schema(description = "该广告位曝光PV", example = "100")
    private Long pv;

    /**
     * 该广告位曝光UV（基于曝光事件去重）
     */
    @Schema(description = "该广告位曝光UV（基于曝光事件去重）", example = "30")
    private Long uv;

    /**
     * 该广告位点击PV
     */
    @Schema(description = "该广告位点击PV", example = "10")
    private Long clicks;

    /**
     * 该广告位点击率 CTR（口径：点击量/曝光量 * 100%，保留两位小数；曝光为0时返回0）
     */
    @Schema(description = "该广告位点击率CTR（点击量/曝光量 * 100%，保留两位小数）", example = "10.00")
    private Double ctr;

}

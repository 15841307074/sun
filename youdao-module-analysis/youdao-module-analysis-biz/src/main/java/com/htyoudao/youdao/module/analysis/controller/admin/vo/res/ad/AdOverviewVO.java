package com.htyoudao.youdao.module.analysis.controller.admin.vo.res.ad;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Schema(description = "广告分析概览数据")
@NoArgsConstructor
public class AdOverviewVO {

    /**
     * 广告事件总访问量（曝光+点击+离开事件总次数）
     */
    @Schema(description = "广告事件总访问量（全部广告事件次数之和）", example = "1024")
    private Long totalPv;

    /**
     * 广告曝光独立访客数（基于曝光事件对用户去重）
     */
    @Schema(description = "广告曝光独立访客数（基于曝光事件去重）", example = "128")
    private Long totalUv;

    /**
     * 广告曝光次数
     */
    @Schema(description = "广告曝光次数", example = "800")
    private Long exposureCount;

    /**
     * 广告点击次数
     */
    @Schema(description = "广告点击次数", example = "96")
    private Long clickCount;

    /**
     * 点击率 CTR（口径：点击量/曝光量 * 100%，保留两位小数；曝光为0时返回0）
     */
    @Schema(description = "点击率CTR（点击量/曝光量 * 100%，保留两位小数）", example = "12.00")
    private Double ctr;

    /**
     * 平均停留时长（毫秒，基于广告离开事件）
     */
    @Schema(description = "平均停留时长（毫秒，基于广告离开事件）", example = "3500")
    private Double avgStayMs;

}

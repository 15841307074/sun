package com.htyoudao.youdao.module.analysis.dal.es;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Schema(description = "批量曝光项")
public class AdExposureItem {

    @Schema(description = "广告ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long adId;

    @Schema(description = "广告名称")
    private String adName;

    @Schema(description = "广告位序号 1-14", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer adInfoPosition;
}

package com.htyoudao.youdao.module.promotion.api.activity.VO;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - 秒杀活动商品  返回 VO")
@Data
public class ActivityJDCommodityRespVO {

    @Schema(description = "id")
    private Long id;

    @Schema(description = "activityId")
    private Long activityId;

    // 商品ID
    @Schema(description = "商品ID")
    private Long commodityId;


    @Schema(description = "商品名称")
    private String commodityName;

}

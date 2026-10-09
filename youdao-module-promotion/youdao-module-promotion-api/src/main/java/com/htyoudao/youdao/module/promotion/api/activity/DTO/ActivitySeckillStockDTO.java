package com.htyoudao.youdao.module.promotion.api.activity.DTO;

import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serializable;
import lombok.Data;

@Schema(description = "管理后台 - 营销活动秒杀商品库存  返回 VO")
@Data
public class ActivitySeckillStockDTO implements Serializable {

    @Schema(description = "商品ID")
    private Long commodityId;

    @Schema(description = "当前场次剩余库存")
    private Integer stock;
}

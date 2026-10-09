package com.htyoudao.youdao.module.commodity.controller.app.product.vo;

import com.htyoudao.youdao.module.commodity.dal.dto.SpuDto;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Data
@Schema(description = "app - 秒杀商品 Response VO")
public class SeckillSpuVO extends SpuDto {

    @Schema(description = "活动库存")
    private Integer activityStock;

    @Schema(description = "单品限购数")
    private Integer limitPerItem;

    @Schema(description = "秒杀价格（小数点后两位）")
    private BigDecimal seckillPrice;

    @Schema(description = "优惠叠加（0不叠加 1叠加）")
    private Integer discountStackable;

    @Schema(description = "1 优惠卷")
    private List<Integer> stackableActivitieList = new ArrayList<>();

}

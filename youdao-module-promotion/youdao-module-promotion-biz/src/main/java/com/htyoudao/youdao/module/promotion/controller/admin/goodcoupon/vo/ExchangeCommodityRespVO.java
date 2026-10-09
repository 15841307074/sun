package com.htyoudao.youdao.module.promotion.controller.admin.goodcoupon.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;
import com.alibaba.excel.annotation.*;

/**
 * @author dht
 */
@Schema(description = "管理后台 - 兑换券下单时必选商品 Response VO")
@Data
public class ExchangeCommodityRespVO {

    @Schema(description = "id", requiredMode = Schema.RequiredMode.REQUIRED, example = "7457")
    private Long id;

    @Schema(description = "优惠券id", requiredMode = Schema.RequiredMode.REQUIRED, example = "21210")
    private Long couponId;

    @Schema(description = "商品id", requiredMode = Schema.RequiredMode.REQUIRED, example = "23994")
    private Long commodityId;

    @Schema(description = "商品id", requiredMode = Schema.RequiredMode.REQUIRED, example = "23994")
    private String commodityName;
}
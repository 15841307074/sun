package com.htyoudao.youdao.module.promotion.controller.app.activityCQ.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "抽签奖品")
public class PrizeVO {

    @Schema(description = "奖品ID")
    private Long id;

    @Schema(description = "奖品类型")
    private Integer prizeType;

    @Schema(description = "奖品名称")
    private String prizeName;

    @Schema(description = "奖品图片")
    private String prizeImgUrl;

    @Schema(description = "奖品价值")
    private BigDecimal prizeValue;

    @Schema(description = "总库存")
    private Integer totalStock;

    @Schema(description = "已发放库存")
    private Integer usedStock;

    @Schema(description = "剩余库存")
    private Integer remainStock;
}

package com.htyoudao.youdao.module.promotion.api.lottery.DTO;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.ToString;

import java.io.Serializable;
import java.util.List;

@Schema(description = "管理后台 - 优惠券分页 Request VO")
@Data
@ToString(callSuper = true)
public class LotteryDTO implements Serializable {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "15444")
    private Long id;

    @Schema(description = "指定商品", example = "1024")
    private List<Long> commodityIds;

}

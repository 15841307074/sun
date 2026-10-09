package com.htyoudao.youdao.module.promotion.controller.app.activityCQ.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "抽签奖品列表")
public class ActivityCqPrizeListVO {

    @Schema(description = "奖品列表")
    private List<PrizeVO> prizeList;
}

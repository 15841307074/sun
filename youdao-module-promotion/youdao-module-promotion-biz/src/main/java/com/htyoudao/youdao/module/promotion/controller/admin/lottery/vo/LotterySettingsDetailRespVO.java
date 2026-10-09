package com.htyoudao.youdao.module.promotion.controller.admin.lottery.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class LotterySettingsDetailRespVO extends LotterySettingsRespVO {


    /** 奖品列表 */
    @Schema(name = "prizes", description = "奖品列表")
    List<LotteryPrizeRespVO> prizes= new ArrayList<>();
    /** 剩余次数 */
    @Schema(name = "frequency", description = "剩余次数")
    Integer  frequency;
}

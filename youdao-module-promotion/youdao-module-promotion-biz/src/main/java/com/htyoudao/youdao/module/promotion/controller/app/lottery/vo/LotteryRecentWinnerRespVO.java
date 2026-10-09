package com.htyoudao.youdao.module.promotion.controller.app.lottery.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "活动最近中奖记录（不包含兜底奖品）")
public class LotteryRecentWinnerRespVO {
    @Schema(description = "已脱敏手机号", example = "159****5152")
    private String memberMobile;
    @Schema(description = "中奖奖品名称", example = "10元优惠券")
    private String prizeName;
}

package com.htyoudao.youdao.module.promotion.controller.admin.lottery.vo;

import com.htyoudao.youdao.module.promotion.controller.app.lottery.vo.LotteryUserLogVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@Schema(description = "管理后台 - 人工补发安排结果")
public class LotteryReissueRespVO {
    @Schema(description = "本次是否重新安排发奖任务；不表示奖品已发放")
    private boolean queued;
    @Schema(description = "安排结果说明")
    private String message;
    @Schema(description = "原请求当前中奖及发奖结果")
    private LotteryUserLogVO result;
}

package com.htyoudao.youdao.module.promotion.controller.app.lottery.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.htyoudao.youdao.module.promotion.controller.admin.lottery.vo.LotterySettingsCacheDataVO;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * pc接取 vo
 */
@Data
public class LotterySettingsResVO {
    LotterySettingsCacheDataVO cacheData;
    /**
     * 奖品列表
     */
    @Schema(name = "prizes", description = "奖品列表")
    private List<LotteryPrizeVO> prizes = new ArrayList<>();

}

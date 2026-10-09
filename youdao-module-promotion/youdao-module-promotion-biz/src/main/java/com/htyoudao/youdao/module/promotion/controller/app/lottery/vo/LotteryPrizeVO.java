package com.htyoudao.youdao.module.promotion.controller.app.lottery.vo;

import com.htyoudao.youdao.module.promotion.dal.dataobject.lottery.LotteryPrizeDO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class LotteryPrizeVO extends LotteryPrizeDO {
    public LotteryPrizeVO(LotteryPrizeDO prize) {
        this.setId(prize.getId());
        this.setLotteryId(prize.getLotteryId());
        this.setPrizeType(prize.getPrizeType());
        this.setPrizeName(prize.getPrizeName());
        this.setPrizeNum(prize.getPrizeNum());
        this.setRemainNum(prize.getRemainNum());
        this.setPrizeValue(prize.getPrizeValue());
        this.setIsRepeat(prize.getIsRepeat());
        this.setAwardId(prize.getAwardId());
        this.setProbability(prize.getProbability());
        this.setPrizeImgUrl(prize.getPrizeImgUrl());
        this.setIsGuarantees(prize.getIsGuarantees());
        this.setWinningCitys(prize.getWinningCitys()!=null?prize.getWinningCitys():"");
        this.setMinimumNumber(prize.getMinimumNumber()!=null?prize.getMinimumNumber():0);
    }

}

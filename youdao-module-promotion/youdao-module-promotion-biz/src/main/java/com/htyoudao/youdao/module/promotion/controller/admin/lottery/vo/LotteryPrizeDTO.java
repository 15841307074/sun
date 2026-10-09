package com.htyoudao.youdao.module.promotion.controller.admin.lottery.vo;

import com.htyoudao.youdao.module.promotion.dal.dataobject.lottery.LotteryPrizeDO;
import lombok.Data;

@Data
public class LotteryPrizeDTO extends LotteryPrizeDO {

    public LotteryPrizeDTO(LotteryPrizeDO prize) {
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
    }

}

package com.htyoudao.youdao.module.promotion.controller.admin.lottery.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StoreStatisticsDTO {
    private Long storeId;
    private String participantsCount;
    private String lotteryCount;
}

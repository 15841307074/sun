package com.htyoudao.youdao.module.promotion.controller.admin.activityJk.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ActivityStoreStatisticsDTO {
    private Long storeId;
    private String participantsCount;
    private String count;
}

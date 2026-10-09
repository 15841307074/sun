package com.htyoudao.youdao.module.promotion.controller.admin.activityAnswer.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 有奖问答参与门店返回。
 */
@Data
public class ActivityAnswerStoreRespVO {

    @Schema(description = "门店ID")
    private Long storeId;

    @Schema(description = "门店名称")
    private String storeName;
}

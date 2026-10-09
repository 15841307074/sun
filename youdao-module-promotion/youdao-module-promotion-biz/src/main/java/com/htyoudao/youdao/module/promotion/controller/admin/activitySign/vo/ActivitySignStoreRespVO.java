package com.htyoudao.youdao.module.promotion.controller.admin.activitySign.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - 签到活动门店 Response VO")
@Data
public class ActivitySignStoreRespVO {

    @Schema(description = "门店ID", example = "101")
    private Long storeId;

    @Schema(description = "门店名称", example = "沈阳工业大学北区一食堂一楼")
    private String storeName;
}

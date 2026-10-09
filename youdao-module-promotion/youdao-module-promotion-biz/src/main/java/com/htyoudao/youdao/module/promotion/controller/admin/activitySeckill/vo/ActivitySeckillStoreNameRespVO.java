package com.htyoudao.youdao.module.promotion.controller.admin.activitySeckill.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "秒杀活动门店返回对象")
public class ActivitySeckillStoreNameRespVO {
    @Schema(description = "门店名称")
    private String storeName;
    @Schema(description = "门店 Id")
    private Long storeId;
}

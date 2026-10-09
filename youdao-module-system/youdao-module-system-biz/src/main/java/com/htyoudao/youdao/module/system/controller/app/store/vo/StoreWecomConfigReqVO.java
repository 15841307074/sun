package com.htyoudao.youdao.module.system.controller.app.store.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
public class StoreWecomConfigReqVO {
    @Schema(description = "门店经度", example = "1024")
    private double longitude;

    @Schema(description = "门店维度", example = "1024")
    private double latitude;

    @Schema(description = "城市名称", example = "1024")
    private String cityName;

    @Schema(description = "门店名", example = "1024")
    private String storeName;

    @Schema(description = "门店id", example = "1024")
    private Long storeId;

    @Schema(description = "优惠卷id", example = "1024")
    private Long couponId;

    @Schema(description = "门店id", example = "1024")
    private List<Long> storeIdList;

    @Schema(description = "会员ID", example = "1024")
    private String memberId;

    @Schema(description = "活动id", example = "1024")
    private Long activityId;
}

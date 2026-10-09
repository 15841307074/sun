package com.htyoudao.youdao.module.system.api.store.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class StoreWecomConfigReqDTO implements Serializable {

    @Schema(description = "优惠卷id", example = "1024")
    private Long couponId;

    @Schema(description = "门店经度", example = "1024")
    private double longitude;

    @Schema(description = "门店维度", example = "1024")
    private double latitude;

    @Schema(description = "城市名称", example = "1024")
    @NotBlank(message = "城市名称不能为空")
    private String cityName;

    @Schema(description = "门店名", example = "1024")
    private String storeName;

    @Schema(description = "门店id", example = "1024")
    private List<Long> storeIdList;
    @Schema(description = "门店id", example = "1024")
    private Long storeId;

}

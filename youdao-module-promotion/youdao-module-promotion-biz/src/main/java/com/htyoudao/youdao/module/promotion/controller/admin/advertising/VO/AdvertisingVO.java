package com.htyoudao.youdao.module.promotion.controller.admin.advertising.VO;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class AdvertisingVO {

    @Schema(description = "门店ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long storeId;

    @Schema(description = "位置列表", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<Integer> locationIds;

    @Schema(description = "会员ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long memberId;

    @Schema(description = "1 微信  2 支付宝", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer appSource;



}

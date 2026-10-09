package com.htyoudao.youdao.module.promotion.controller.admin.activityJD.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Schema(description = "管理后台 - 秒杀活动 商品新增/修改 Request VO")
@Data
public class ActivityJDCommoditySaveReqVO {

    @Schema(description = "ID")
    private Long id;

    // 商品ID
    @Schema(description = "商品ID")
    @NotNull(message = "商品ID 不能为空")
    private Long commodityId;

    @Schema(description = "商品名称")
    @NotNull(message = "商品名称 不能为空")
    private String commodityName;


}

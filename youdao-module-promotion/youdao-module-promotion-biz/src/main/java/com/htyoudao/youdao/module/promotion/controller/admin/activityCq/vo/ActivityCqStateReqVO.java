package com.htyoudao.youdao.module.promotion.controller.admin.activityCq.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ActivityCqStateReqVO {

    /**
     * 活动id
     */

    @Schema(description = "活动id")
    @NotNull
    private Long id;

    /**
     * 是否启用
     */

    @Schema(description = "是否启用")
    @NotNull
    private Integer isEnabled;
}
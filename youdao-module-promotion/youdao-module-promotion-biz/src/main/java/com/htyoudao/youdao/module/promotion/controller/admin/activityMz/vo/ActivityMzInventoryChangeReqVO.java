package com.htyoudao.youdao.module.promotion.controller.admin.activityMz.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 满赠活动赠品库存变更 Request VO
 */
@Schema(description = "管理后台 - 满赠活动赠品库存变更 Request VO")
@Data
public class ActivityMzInventoryChangeReqVO {

    @Schema(description = "活动ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "活动ID不能为空")
    private Long activityId;

    @Schema(description = "门店ID（独立库存模式必传，共用库存模式不传）")
    private Long storeId;

    @Schema(description = "赠送商品ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "赠送商品ID不能为空")
    private Long giftCommodityId;

    @Schema(description = "变化量（负数=扣减，正数=增加/回滚）", requiredMode = Schema.RequiredMode.REQUIRED, example = "-1")
    @NotNull(message = "变化量不能为空")
    private Integer change;
}

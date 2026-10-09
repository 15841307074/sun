package com.htyoudao.youdao.module.promotion.controller.admin.activitySeckill.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Schema(description = "秒杀活动开启关闭 VO")
@Data
public class ActivitySeckillEnabledUpdateReqVO {
    @Schema(description = "活动 id")
    @NotNull(message = "活动 id 不能为空")
    private Long id;

    @Schema(description = "开启状态 1是 0否")
    @NotNull(message = "开启状态 1是 0否 不能为空")
    private Integer isEnabled;
}

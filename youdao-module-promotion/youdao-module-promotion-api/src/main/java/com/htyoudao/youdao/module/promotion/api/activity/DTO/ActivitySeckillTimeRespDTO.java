package com.htyoudao.youdao.module.promotion.api.activity.DTO;

import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serializable;
import lombok.Data;

@Schema(description = "管理后台 - 秒杀活动场次  返回 VO")
@Data
public class ActivitySeckillTimeRespDTO  implements Serializable {
    @Schema(description = "id")
    private Long id;

    @Schema(description = "activityId")
    private Long activityId;

    @Schema(description = "场次")
    private Integer times;

    @Schema(description = "开始时间")
    private Integer startTime;

    @Schema(description = "结束时间")
    private Integer endTime;
}

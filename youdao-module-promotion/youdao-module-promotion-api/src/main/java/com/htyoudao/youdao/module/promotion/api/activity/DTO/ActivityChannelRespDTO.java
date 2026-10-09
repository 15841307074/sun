package com.htyoudao.youdao.module.promotion.api.activity.DTO;

import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serializable;
import lombok.Data;

@Data
@Schema(description = "活动渠道列表返回 VO")
public class ActivityChannelRespDTO  implements Serializable {

    // 主键ID
    @Schema(description = "id")
    private Long id;
    // 活动ID（关联秒杀活动表）
    @Schema(description = "活动 id")
    private Long activityId;
    // 渠道名称
    @Schema(description = " 渠道名称")
    private String channelName;

    // 链接地址
    @Schema(description = "链接地址")
    private String linkUrl;
}

package com.htyoudao.youdao.module.promotion.api.activity.VO;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "活动渠道列表返回 VO")
public class ActivityChannelRespVO {

    // 主键ID
    @Schema(description = "id")
    private Integer id;
    // 活动ID（关联秒杀活动表）
    @Schema(description = "活动 id")
    private Long activityId;
    // 渠道名称
    @Schema(description = " 渠道名称")
    private String channelName;

    // 链接地址
    @Schema(description = "链接地址")
    private String linkUrl;
    @Schema(description = "是否默认 1是 0否")
    private Integer isDefault;

    //渠道 ID
    private Long channelId;
}

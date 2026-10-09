package com.htyoudao.youdao.module.promotion.api.activity.VO;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@Schema(description = "集点活动推广详情 VO")
public class ActivityJDSpreadRespVO {

    @Schema(description = "活动 id")
    private Long id;

    // 分享图片
    @Schema(description = "分享图片")
    private String shareImageUrl;

    // 分享标题
    @Schema(description = "分享标题")
    private String shareTitle;

    // 分享描述
    @Schema(description = "分享描述")
    private String shareDescription;

    @Schema(description = "活动推广渠道链接 集合")
    private List<ActivityChannelRespVO> activityChannelRespVOS = new ArrayList<>();
}

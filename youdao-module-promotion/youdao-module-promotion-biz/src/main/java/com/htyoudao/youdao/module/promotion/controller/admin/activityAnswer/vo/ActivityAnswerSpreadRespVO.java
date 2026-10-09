package com.htyoudao.youdao.module.promotion.controller.admin.activityAnswer.vo;

import com.htyoudao.youdao.module.promotion.api.activity.VO.ActivityChannelRespVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@Schema(description = "有奖问答活动推广详情")
public class ActivityAnswerSpreadRespVO {

    @Schema(description = "活动 ID")
    private Long id;

    @Schema(description = "分享标题")
    private String shareTitle;

    @Schema(description = "分享内容")
    private String shareNote;

    @Schema(description = "分享图片")
    private String shareImgUrl;

    @Schema(description = "活动推广渠道链接集合")
    private List<ActivityChannelRespVO> activityChannelRespVOS = new ArrayList<>();
}

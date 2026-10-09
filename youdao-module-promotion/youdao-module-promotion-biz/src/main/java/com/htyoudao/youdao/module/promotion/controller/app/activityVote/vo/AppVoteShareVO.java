package com.htyoudao.youdao.module.promotion.controller.app.activityVote.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class AppVoteShareVO {

    @Schema(description = "活动id")
    private Long activityId;

    @Schema(description = "分享标题")
    private String shareTitle;

    @Schema(description = "分享内容")
    private String shareNote;

    @Schema(description = "分享图片")
    private String shareImgUrl;

    @Schema(description = "分享类型 1不允许转发 2允许转发好友 3允许复制链接")
    private Integer shareType;
}

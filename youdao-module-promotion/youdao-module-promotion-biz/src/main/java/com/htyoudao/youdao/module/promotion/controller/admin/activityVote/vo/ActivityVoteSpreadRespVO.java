package com.htyoudao.youdao.module.promotion.controller.admin.activityVote.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class ActivityVoteSpreadRespVO {

    @Schema(description = "活动id")
    private Long id;

    @Schema(description = "分享标题")
    private String shareTitle;

    @Schema(description = "分享内容")
    private String shareNote;

    @Schema(description = "分享图片")
    private String shareImgUrl;

    @Schema(description = "分享类型")
    private Integer shareType;
}

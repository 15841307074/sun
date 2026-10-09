package com.htyoudao.youdao.module.promotion.controller.app.activityJD.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class ActivityCollectAppShareVO {

    //id
    @Schema(description = "id")
    private Long id;
    //activityId
    @Schema(description = "activityId")
    private Long activityId;
    // 分享图片
    @Schema(description = "分享图片")
    private String shareImgUrl;
    // 分享标题
    @Schema(description = "分享标题")
    private String shareTitle;
    // 分享描述
    @Schema(description = "分享描述")
    private String shareNote;
}

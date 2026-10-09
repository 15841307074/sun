package com.htyoudao.youdao.module.promotion.controller.admin.activityCq.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class ActivityCqSpreadRespVO {

    /**
     * 活动id
     */

    @Schema(description = "活动id")
    private Long id;

    /**
     * 分享标题
     */

    @Schema(description = "分享标题")
    private String shareTitle;

    /**
     * 分享内容
     */

    @Schema(description = "分享内容")
    private String shareNote;

    /**
     * 分享图片
     */

    @Schema(description = "分享图片")
    private String shareImgUrl;
}
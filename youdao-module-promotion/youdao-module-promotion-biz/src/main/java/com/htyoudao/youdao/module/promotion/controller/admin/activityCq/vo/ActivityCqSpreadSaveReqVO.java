package com.htyoudao.youdao.module.promotion.controller.admin.activityCq.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ActivityCqSpreadSaveReqVO {

    /**
     * 活动id
     */

    @Schema(description = "活动id")
    @NotNull
    private Long id;

    /**
     * 分享标题
     */

    @Schema(description = "分享标题")
    @NotBlank
    private String shareTitle;

    /**
     * 分享内容
     */

    @Schema(description = "分享内容")
    @NotBlank
    private String shareNote;

    /**
     * 分享图片
     */

    @Schema(description = "分享图片")
    @NotBlank
    private String shareImgUrl;
}
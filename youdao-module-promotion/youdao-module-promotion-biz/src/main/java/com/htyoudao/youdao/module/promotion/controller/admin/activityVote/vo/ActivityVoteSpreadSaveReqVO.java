package com.htyoudao.youdao.module.promotion.controller.admin.activityVote.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ActivityVoteSpreadSaveReqVO {

    @Schema(description = "活动id")
    @NotNull(message = "活动id不能为空")
    private Long id;

    @Schema(description = "分享标题")
    @NotBlank(message = "分享标题不能为空")
    private String shareTitle;

    @Schema(description = "分享内容")
    @NotBlank(message = "分享内容不能为空")
    private String shareNote;

    @Schema(description = "分享图片")
    @NotBlank(message = "分享图片不能为空")
    private String shareImgUrl;

    @Schema(description = "分享类型")
    private Integer shareType;
}

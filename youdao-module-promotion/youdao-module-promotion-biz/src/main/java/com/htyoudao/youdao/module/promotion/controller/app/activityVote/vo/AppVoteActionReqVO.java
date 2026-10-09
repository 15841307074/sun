package com.htyoudao.youdao.module.promotion.controller.app.activityVote.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AppVoteActionReqVO {

    @Schema(description = "活动id")
    @NotNull(message = "活动id不能为空")
    private Long activityId;

    @Schema(description = "选项id")
    @NotNull(message = "选项id不能为空")
    private Long optionId;

    @Schema(description = "门店id")
    private Long storeId;

    @Schema(description = "门店名称（前端传入）")
    private String storeName;

    @Schema(description = "会员昵称（前端传入）")
    private String memberNickName;

    @Schema(description = "会员id（后端填充）")
    private Long memberId;

    @Schema(description = "会员手机号（后端填充，投票唯一标识）")
    private Long memberMobile;
}

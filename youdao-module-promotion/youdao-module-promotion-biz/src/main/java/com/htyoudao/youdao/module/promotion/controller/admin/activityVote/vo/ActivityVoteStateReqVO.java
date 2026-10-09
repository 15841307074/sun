package com.htyoudao.youdao.module.promotion.controller.admin.activityVote.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ActivityVoteStateReqVO {

    @Schema(description = "活动id")
    @NotNull(message = "活动id不能为空")
    private Long id;

    @Schema(description = "状态 0停用 1启用")
    @NotNull(message = "状态不能为空")
    private Integer isEnabled;
}

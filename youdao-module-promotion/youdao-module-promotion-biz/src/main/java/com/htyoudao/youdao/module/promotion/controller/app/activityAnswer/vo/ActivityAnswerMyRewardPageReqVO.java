package com.htyoudao.youdao.module.promotion.controller.app.activityAnswer.vo;

import com.htyoudao.youdao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 有奖问答我的奖励分页请求。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ActivityAnswerMyRewardPageReqVO extends PageParam {

    @Schema(description = "活动ID")
    @NotNull(message = "活动ID不能为空")
    private Long activityId;

    @Schema(description = "门店ID")
    private Long storeId;

    @Schema(description = "会员ID，后端以登录态覆盖")
    private Long memberId;

    @Schema(description = "奖品类型")
    private Integer prizeType;

    @Schema(description = "红包领取状态 1未领取 2已领取 3已失效")
    private Integer claimStatus;
}

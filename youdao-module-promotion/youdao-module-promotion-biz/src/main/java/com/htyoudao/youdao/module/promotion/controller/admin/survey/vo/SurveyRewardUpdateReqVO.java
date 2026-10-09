package com.htyoudao.youdao.module.promotion.controller.admin.survey.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Schema(description = "管理后台 - 问卷奖励配置更新 Request VO")
@Data
public class SurveyRewardUpdateReqVO {

    @Schema(description = "问卷ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "问卷ID不能为空")
    private Long id;

    @Schema(description = "奖励类型：0-无 1-积分 2-优惠券 3-优惠券包")
    @NotNull(message = "奖励类型不能为空")
    private Integer rewardType;

    @Schema(description = "积分数")
    private Integer rewardPoints;

    @Schema(description = "优惠券ID")
    private Long couponId;

    @Schema(description = "优惠券名称")
    private String couponName;

    @Schema(description = "发放方式：1-立即发放 2-按条件发放")
    private Integer grantMode;

    @Schema(description = "发放条件描述")
    private String grantCondition;
}

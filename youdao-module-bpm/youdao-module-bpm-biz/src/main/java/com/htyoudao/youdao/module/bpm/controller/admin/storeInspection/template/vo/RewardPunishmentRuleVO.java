package com.htyoudao.youdao.module.bpm.controller.admin.storeInspection.template.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "奖惩规则")
public class RewardPunishmentRuleVO {

    /**
     * 奖惩开关：0-不填(默认) 1-填写
     */
    private Integer rewardPunishmentFlag = 0;

    /**
     * 合格时：0-选填(默认) 1-必填
     */
    private Integer qualified = 0;

    /**
     * 不合格时：0-选填(默认) 1-必填
     */
    private Integer unqualified = 0;

}

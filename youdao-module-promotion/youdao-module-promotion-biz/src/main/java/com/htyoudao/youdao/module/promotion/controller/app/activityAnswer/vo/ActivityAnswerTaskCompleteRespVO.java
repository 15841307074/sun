package com.htyoudao.youdao.module.promotion.controller.app.activityAnswer.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 有奖问答任务完成响应。
 */
@Data
public class ActivityAnswerTaskCompleteRespVO {

    @Schema(description = "是否完成成功")
    private Boolean success;

    @Schema(description = "本次新增答题次数")
    private Integer addChance;

    @Schema(description = "完成后的可用答题次数")
    private Integer chanceCount;

    @Schema(description = "提示文案")
    private String message;
}

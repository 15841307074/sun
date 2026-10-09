package com.htyoudao.youdao.module.promotion.controller.admin.activityAnswer.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ActivityAnswerStateReqVO {

    @Schema(description = "有奖问答配置ID")
    @NotNull(message = "id不能为空")
    private Long id;

    @Schema(description = "是否启用")
    @NotNull
    private Integer isEnabled;
}

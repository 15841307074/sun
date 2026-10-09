package com.htyoudao.youdao.module.promotion.controller.app.activityAnswer.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 取消答题请求。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ActivityAnswerCancelReqVO extends ActivityAnswerBaseReqVO {

    @Schema(description = "答题记录ID")
    @NotNull(message = "答题记录ID不能为空")
    private Long recordId;
}

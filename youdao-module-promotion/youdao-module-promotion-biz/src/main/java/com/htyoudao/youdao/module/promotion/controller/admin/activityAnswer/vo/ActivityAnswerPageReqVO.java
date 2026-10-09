package com.htyoudao.youdao.module.promotion.controller.admin.activityAnswer.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.htyoudao.youdao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
public class ActivityAnswerPageReqVO extends PageParam {

    @Schema(description = "活动标题")
    private String answerTitle;

    @Schema(description = "状态 0禁用 1启用")
    private Integer state;

    @Schema(description = "开始时间")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private LocalDate startTime;

    @Schema(description = "结束时间")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private LocalDate endTime;
}

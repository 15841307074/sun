package com.htyoudao.youdao.module.promotion.controller.admin.survey.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - 问卷修改状态 Request VO")
@Data
public class SurveyUpdateStatusReqVO {

    @Schema(description = "问卷ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "问卷ID不能为空")
    private Long id;

    @Schema(description = "状态：1-发布 2-停用", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "状态不能为空")
    private Integer status;

    @Schema(description = "开始时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime startTime;

    @Schema(description = "结束时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime endTime;
}

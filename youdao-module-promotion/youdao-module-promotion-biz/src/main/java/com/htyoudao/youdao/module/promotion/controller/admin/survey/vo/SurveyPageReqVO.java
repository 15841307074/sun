package com.htyoudao.youdao.module.promotion.controller.admin.survey.vo;

import com.htyoudao.youdao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - 问卷分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class SurveyPageReqVO extends PageParam {

    @Schema(description = "问卷名称（模糊匹配）")
    private String surveyName;

    @Schema(description = "状态（计算字段）：0-未发布 1-进行中 2-已结束")
    private Integer status;

    @Schema(description = "发布时间起始")
    private LocalDateTime startTimeBegin;

    @Schema(description = "发布时间截止")
    private LocalDateTime startTimeEnd;

    @Schema(description = "结束时间起始")
    private LocalDateTime endTimeBegin;

    @Schema(description = "结束时间截止")
    private LocalDateTime endTimeEnd;
}

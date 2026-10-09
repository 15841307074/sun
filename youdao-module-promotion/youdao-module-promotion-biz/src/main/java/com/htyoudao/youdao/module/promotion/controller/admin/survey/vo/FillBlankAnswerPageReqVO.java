package com.htyoudao.youdao.module.promotion.controller.admin.survey.vo;

import com.htyoudao.youdao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Schema(description = "管理后台 - 填空题答案分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class FillBlankAnswerPageReqVO extends PageParam {

    @Schema(description = "问卷ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long surveyId;

    @Schema(description = "题目ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long questionId;

    @Schema(description = "搜索关键词（匹配答案文本）")
    private String keyword;

    @Schema(description = "是否过滤空答案")
    private Boolean filterEmpty;
}

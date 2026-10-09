package com.htyoudao.youdao.module.promotion.controller.app.survey.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Schema(description = "小程序 - 提交答卷 Request VO")
@Data
public class AppSurveySubmitReqVO {

    @Schema(description = "问卷ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "问卷ID不能为空")
    private Long surveyId;

    @Schema(description = "答题列表")
    @NotNull(message = "答题列表不能为空")
    private List<AnswerVO> answers;

    @Schema(description = "来源：1-微信 2-支付宝", example = "1")
    private Integer source;

    @Schema(description = "答题所用秒数", example = "120")
    private Integer duration;

    @Schema(description = "答题VO")
    @Data
    public static class AnswerVO {

        @Schema(description = "题目ID")
        @NotNull(message = "题目ID不能为空")
        private Long questionId;

        @Schema(description = "题目序号")
        private Integer questionNo;

        @Schema(description = "选中的选项ID列表")
        private List<Long> selectedOptionIds;

        @Schema(description = "填空文本列表：选择题带填空时每项用 optionId 标识选项；问答题时只传 text，optionId 不传")
        private List<FillBlankItem> fillBlankText;
    }

    @Schema(description = "填空文本项")
    @Data
    public static class FillBlankItem {

        @Schema(description = "选项ID，仅【选择题带填空】场景使用，标识该填空文本属于哪个选项；【问答题】场景不传", example = "300010")
        private String optionId;

        @Schema(description = "填空文本", example = "苹果")
        private String text;
    }
}

package com.htyoudao.youdao.module.promotion.controller.admin.survey.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Schema(description = "管理后台 - 问卷统计 Response VO")
@Data
public class SurveyStatsRespVO {

    @Schema(description = "问卷ID")
    private Long surveyId;

    @Schema(description = "总答卷数")
    private Long totalAnswers;

    @Schema(description = "UV访问量")
    private Long uvCount;

    @Schema(description = "题目统计列表")
    private List<QuestionStatsVO> questions;

    @Data
    public static class QuestionStatsVO {

        @Schema(description = "题目ID")
        private Long questionId;

        @Schema(description = "题目序号")
        private Integer questionNo;

        @Schema(description = "题目标题")
        private String questionTitle;

        @Schema(description = "题目类型")
        private Integer questionType;

        @Schema(description = "题目类型文本")
        private String questionTypeText;

        @Schema(description = "有效回答数")
        private Integer validCount;

        @Schema(description = "选项统计列表")
        private List<OptionStatsVO> options;
    }

    @Data
    public static class OptionStatsVO {

        @Schema(description = "选项ID")
        private Long optionId;

        @Schema(description = "选项文本")
        private String optionText;

        @Schema(description = "选择人数")
        private Long count;

        @Schema(description = "占比（百分比）")
        private Double percent;
    }
}

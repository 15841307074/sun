package com.htyoudao.youdao.module.promotion.controller.admin.survey.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "管理后台 - 答卷详情 Response VO")
@Data
public class SurveyAnswerDetailRespVO {

    @Schema(description = "答卷ID")
    private Long answerId;

    @Schema(description = "问卷ID")
    private Long surveyId;

    @Schema(description = "手机号")
    private Long phone;

    @Schema(description = "来源：1-微信 2-支付宝")
    private Integer source;

    @Schema(description = "来源文本")
    private String sourceText;

    @Schema(description = "来源IP")
    private String sourceIp;

    @Schema(description = "答题所用秒数")
    private Integer duration;

    @Schema(description = "提交时间")
    private LocalDateTime submitTime;

    @Schema(description = "是否有效")
    private Integer isValid;

    @Schema(description = "奖励状态")
    private Integer rewardStatus;

    @Schema(description = "奖励类型")
    private Integer rewardType;

    @Schema(description = "奖励类型文本")
    private String rewardTypeText;

    @Schema(description = "奖励值")
    private String rewardValue;

    @Schema(description = "奖励发放时间")
    private LocalDateTime rewardGrantedAt;

    @Schema(description = "答题明细列表")
    private List<DetailVO> details;

    @Data
    public static class DetailVO {

        @Schema(description = "题目序号")
        private Integer questionNo;

        @Schema(description = "题目ID")
        private Long questionId;

        @Schema(description = "题目标题")
        private String questionTitle;

        @Schema(description = "题目类型：1-单选 2-多选 3-填空")
        private Integer questionType;

        @Schema(description = "是否必答：0-否 1-是")
        private Integer isRequired;

        @Schema(description = "答案文本（填空/选项文本汇总）")
        private String answerText;

        @Schema(description = "选中的选项ID列表")
        private List<Long> selectedOptionIds;

        @Schema(description = "填空文本列表")
        private List<FillBlankItem> fillBlankText;
    }

    @Schema(description = "填空文本项")
    @Data
    public static class FillBlankItem {

        @Schema(description = "选项ID")
        private String optionId;

        @Schema(description = "填空文本")
        private String text;
    }
}

package com.htyoudao.youdao.module.promotion.controller.app.survey.vo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Schema(description = "小程序 - 问卷详情 Response VO")
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class AppSurveyRespVO {

    @Schema(description = "问卷ID")
    private Long id;

    @Schema(description = "问卷名称")
    private String surveyName;

    @Schema(description = "问卷描述")
    private String surveyDesc;

    @Schema(description = "封面图URL")
    private String coverImageUrl;

    @Schema(description = "状态")
    private Integer status;

    @Schema(description = "状态文本")
    private String statusText;

    @Schema(description = "时间控制：0-创建成功后即可填写（无结束时间） 1-按开始/结束时间控制")
    private Integer timeControl;

    @Schema(description = "开始时间")
    private String startTime;

    @Schema(description = "开始时间开关")
    private Integer startTimeEnabled;

    @Schema(description = "结束时间")
    private String endTime;

    @Schema(description = "结束时间开关")
    private Integer endTimeEnabled;

    @Schema(description = "是否需要登录")
    private Integer needLogin;

    @Schema(description = "是否允许重复")
    private Integer allowRepeat;

    @Schema(description = "最大重复提交次数（allowRepeat!=0 时有效）")
    private Integer repeatLimit;

    @Schema(description = "当前用户是否已达到提交次数上限")
    private Boolean submitLimitReached;

    @Schema(description = "提示消息（未开始/已结束时）")
    private String hintMessage;

    @Schema(description = "提交后跳转页面地址")
    private String redirectUrl;

    @Schema(description = "题目列表")
    private List<QuestionVO> questions;

    @Data
    public static class QuestionVO {
        @Schema(description = "题目ID")
        private Long id;
        @Schema(description = "题目序号")
        private Integer questionNo;
        @Schema(description = "题目标题")
        private String questionTitle;
        @Schema(description = "题目提示")
        private String questionHint;
        @Schema(description = "题目类型：1-单选 2-多选 3-填空")
        private Integer questionType;
        @Schema(description = "题目类型文本")
        private String questionTypeText;
        @Schema(description = "题目配图")
        private String questionImage;
        @Schema(description = "是否必答")
        private Integer isRequired;
        @Schema(description = "是否显示题目提示")
        private Integer showHint;
        @Schema(description = "选项列表")
        private List<OptionVO> options;
    }

    @Data
    public static class OptionVO {
        @Schema(description = "选项ID")
        private Long id;
        @Schema(description = "选项文本")
        private String optionText;
        @Schema(description = "选项说明")
        private String optionDesc;
        @Schema(description = "选项配图")
        private String optionImage;
        @Schema(description = "是否允许填空")
        private Integer allowFillBlank;
    }
}

package com.htyoudao.youdao.module.promotion.controller.admin.survey.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Schema(description = "管理后台 - 问卷新增/修改 Request VO")
@Data
public class SurveySaveReqVO {

    @Schema(description = "问卷ID（更新时必传）")
    private Long id;

    @Schema(description = "问卷名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "问卷名称不能为空")
    @Size(max = 60, message = "问卷名称不能超过30字")
    private String surveyName;

    @Schema(description = "问卷描述")
    @Size(max = 2000, message = "问卷描述不能超过1000字")
    private String surveyDesc;

    @Schema(description = "问卷封面图URL")
    private String coverImageUrl;

    @Schema(description = "时间控制：0-创建成功后即可填写（无结束时间） 1-按开始/结束时间控制")
    private Integer timeControl;

    @Schema(description = "开始时间")
    private String startTime;

    @Schema(description = "开始时间开关：0-关闭 1-开启（开启后 startTime 生效）")
    private Integer startTimeEnabled;

    @Schema(description = "结束时间")
    private String endTime;

    @Schema(description = "结束时间开关：0-关闭 1-开启（开启后 endTime 生效）")
    private Integer endTimeEnabled;

    @Schema(description = "未到开始时间提示语")
    @Size(max = 100, message = "提示语不能超过50字")
    private String startHint;

    @Schema(description = "已结束提示语")
    @Size(max = 100, message = "提示语不能超过50字")
    private String endHint;

    @Schema(description = "提交成功文案")
    private String submitSuccessText;

    @Schema(description = "是否需要登录后答题：0-否 1-是")
    private Integer needLogin;

    @Schema(description = "是否允许重复答卷：0-否 1-是")
    private Integer allowRepeat;

    @Schema(description = "重复答卷上限次数")
    private Integer repeatLimit;

    @Schema(description = "社群专享：0-否 1-是")
    private Integer communityOnly;

    @Schema(description = "提交后显示感谢信息：0-否 1-是")
    private Integer showThanks;

    @Schema(description = "感谢信息文案")
    private String thanksText;

    @Schema(description = "提交后跳转页面地址")
    private String redirectUrl;

    @Schema(description = "店长企微码引导图URL")
    private String guideImageQr;

    @Schema(description = "门店群活码引导图URL")
    private String guideImageGroup;

    @Schema(description = "奖励类型：0-无 1-积分 2-优惠券 3-优惠券包")
    private Integer rewardType;

    @Schema(description = "积分数")
    private Integer rewardPoints;

    @Schema(description = "优惠券ID")
    private Long couponId;

    @Schema(description = "优惠券名称")
    private String couponName;

    @Schema(description = "发放方式：1-立即发放 2-按条件发放")
    private Integer grantMode;

    @Schema(description = "发放条件描述")
    private String grantCondition;

    @Schema(description = "题目列表")
    private List<QuestionVO> questions;

    @Schema(description = "题目VO")
    @Data
    public static class QuestionVO {

        @Schema(description = "题目标题")
        @NotBlank(message = "题目标题不能为空")
        @Size(max = 200, message = "题目标题不能超过100字")
        private String questionTitle;

        @Schema(description = "题目提示")
        private String questionHint;

        @Schema(description = "题目类型：1-单选题 2-多选题 3-填空题")
        private Integer questionType;

        @Schema(description = "题目配图URL")
        private String questionImage;

        @Schema(description = "是否必答：0-否 1-是")
        private Integer isRequired;

        @Schema(description = "是否显示题目提示：0-否 1-是（开启后 questionHint 生效）")
        private Integer showHint;

        @Schema(description = "选项列表")
        private List<OptionVO> options;
    }

    @Schema(description = "选项VO")
    @Data
    public static class OptionVO {

        @Schema(description = "选项文本")
        @NotBlank(message = "选项文本不能为空")
        @Size(max = 100, message = "选项文本不能超过50字")
        private String optionText;

        @Schema(description = "选项说明文字")
        private String optionDesc;

        @Schema(description = "选项配图URL")
        private String optionImage;

        @Schema(description = "是否允许填空")
        private Integer allowFillBlank;
    }
}

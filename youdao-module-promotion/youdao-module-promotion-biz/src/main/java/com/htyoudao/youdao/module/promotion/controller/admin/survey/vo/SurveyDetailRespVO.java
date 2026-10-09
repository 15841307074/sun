package com.htyoudao.youdao.module.promotion.controller.admin.survey.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "管理后台 - 问卷详情 Response VO")
@Data
public class SurveyDetailRespVO {

    @Schema(description = "问卷ID")
    private Long id;

    @Schema(description = "问卷名称")
    private String surveyName;

    @Schema(description = "问卷描述")
    private String surveyDesc;

    @Schema(description = "封面图URL")
    private String coverImageUrl;

    @Schema(description = "状态：0-未发布 1-进行中 2-已结束")
    private Integer status;

    @Schema(description = "时间控制：0-创建成功后即可填写（无结束时间） 1-按开始/结束时间控制")
    private Integer timeControl;

    @Schema(description = "开始时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime startTime;

    @Schema(description = "开始时间开关")
    private Integer startTimeEnabled;

    @Schema(description = "结束时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime endTime;

    @Schema(description = "结束时间开关")
    private Integer endTimeEnabled;

    @Schema(description = "未到开始时间提示语")
    private String startHint;

    @Schema(description = "已结束提示语")
    private String endHint;

    @Schema(description = "提交成功文案")
    private String submitSuccessText;

    @Schema(description = "是否需要登录")
    private Integer needLogin;

    @Schema(description = "是否允许重复答卷")
    private Integer allowRepeat;

    @Schema(description = "重复答卷上限")
    private Integer repeatLimit;

    @Schema(description = "社群专享")
    private Integer communityOnly;

    @Schema(description = "显示感谢信息")
    private Integer showThanks;

    @Schema(description = "感谢文案")
    private String thanksText;

    @Schema(description = "跳转URL")
    private String redirectUrl;

    @Schema(description = "企微码引导图")
    private String guideImageQr;

    @Schema(description = "群活码引导图")
    private String guideImageGroup;

    @Schema(description = "访问UV")
    private Integer uvCount;

    @Schema(description = "提交人数")
    private Integer submitCount;

    @Schema(description = "奖励类型")
    private Integer rewardType;

    @Schema(description = "积分数")
    private Integer rewardPoints;

    @Schema(description = "优惠券ID")
    private Long couponId;

    @Schema(description = "优惠券名称")
    private String couponName;

    @Schema(description = "发放方式")
    private Integer grantMode;

    @Schema(description = "发放条件")
    private String grantCondition;

    @Schema(description = "分享链接")
    private String shareLink;

    @Schema(description = "二维码URL")
    private String shareQrcodeUrl;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

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
        @Schema(description = "题目类型")
        private Integer questionType;
        @Schema(description = "题目配图")
        private String questionImage;
        @Schema(description = "是否必答")
        private Integer isRequired;
        @Schema(description = "是否显示题目提示（开启后 questionHint 生效）")
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

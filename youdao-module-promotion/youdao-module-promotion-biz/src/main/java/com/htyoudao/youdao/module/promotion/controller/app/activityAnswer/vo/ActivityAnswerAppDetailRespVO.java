package com.htyoudao.youdao.module.promotion.controller.app.activityAnswer.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 有奖问答活动详情响应。
 */
@Data
public class ActivityAnswerAppDetailRespVO {

    @Schema(description = "活动ID")
    private Long activityId;

    @Schema(description = "活动标题")
    private String answerTitle;

    @Schema(description = "活动规则")
    private String answerRule;

    @Schema(description = "活动状态 0未开始 1进行中 2已结束 3不在当前场次")
    private Integer activityStatus;

    @Schema(description = "活动状态文案")
    private String statusText;

    @Schema(description = "当前可用答题次数")
    private Integer chanceCount;

    @Schema(description = "同场次未完成答题记录ID")
    private Long unfinishedRecordId;

    @Schema(description = "答题按钮状态 0不可答题 1开始答题 2继续答题  ")
    private Integer answerAction;

    @Schema(description = "答题按钮文案")
    private String answerActionText;

    @Schema(description = "当前场次/周期标识")
    private String currentPeriodKey;

    @Schema(description = "题目总数")
    private Integer questionCount;

    @Schema(description = "当前场次答题记录ID")
    private Long currentRecordId;

    @Schema(description = "答题状态 0未完成 1已完成")
    private Integer answerStatus;

    @Schema(description = "已答题数量")
    private Integer answeredCount;

    @Schema(description = "正确数量")
    private Integer correctCount;

    @Schema(description = "错误数量")
    private Integer wrongCount;

    @Schema(description = "正确率")
    private BigDecimal accuracy;

    @Schema(description = "活动封面图")
    private String activityImgUrl;

    @Schema(description = "活动背景图")
    private String activityBackground;

    @Schema(description = "开始答题按钮图")
    private String buttonImgUrl;

    @Schema(description = "活动详情长图")
    private String activityDetailLongImage;

    @Schema(description = "答题页面背景图")
    private String questionBackgroundImage;

    @Schema(description = "未选择选项图")
    private String unselectedOptionImage;

    @Schema(description = "已选择选项图")
    private String selectedOptionImage;

    @Schema(description = "上一题按钮图")
    private String previousButtonImage;

    @Schema(description = "下一题按钮图")
    private String nextButtonImage;

    @Schema(description = "确认提交按钮图")
    private String submitButtonImage;

    @Schema(description = "分享标题")
    private String shareTitle;

    @Schema(description = "分享内容")
    private String shareNote;

    @Schema(description = "分享图片")
    private String shareImgUrl;

    @Schema(description = "分享类型")
    private Integer shareType;

    @Schema(description = "背景颜色")
    private String backgroundColor;

    @Schema(description = "题目列表，包含正确答案")
    private List<ActivityAnswerQuestionRespVO> questions;

    @Schema(description = "当前场次作答明细")
    private List<ActivityAnswerDetailRecordRespVO> answerDetails;

    @Schema(description = "任务配置")
    private ActivityAnswerTaskConfigRespVO taskConfig;

    @Schema(description = "任务列表")
    private List<ActivityAnswerTaskRespVO> taskList;
}

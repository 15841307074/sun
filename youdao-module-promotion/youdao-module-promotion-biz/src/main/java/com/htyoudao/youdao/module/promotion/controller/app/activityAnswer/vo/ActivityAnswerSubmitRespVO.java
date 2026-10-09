package com.htyoudao.youdao.module.promotion.controller.app.activityAnswer.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 有奖问答提交响应。
 */
@Data
public class ActivityAnswerSubmitRespVO {

    @Schema(description = "活动ID")
    private Long activityId;

    @Schema(description = "活动类型，9有奖问答")
    private Integer activityType;

    @Schema(description = "答题记录ID")
    private Long recordId;

    @Schema(description = "答题编号")
    private String answerNo;

    @Schema(description = "本题是否回答正确")
    private Boolean isCorrect;

    @Schema(description = "是否已答完全部题目")
    private Boolean finished;

    @Schema(description = "已答题数量")
    private Integer answeredCount;

    @Schema(description = "下一题ID，已答完时为空")
    private Long nextQuestionId;

    @Schema(description = "题目总数")
    private Integer questionCount;

    @Schema(description = "正确数量")
    private Integer correctCount;

    @Schema(description = "错误数量")
    private Integer wrongCount;

    @Schema(description = "正确率")
    private BigDecimal accuracy;

    @Schema(description = "每题对错明细")
    private List<ActivityAnswerSubmitDetailRespVO> details;

    @Schema(description = "是否获得奖励")
    private Boolean hasReward;

    @Schema(description = "奖励发放记录ID")
    private Long rewardLogId;

    @Schema(description = "实际命中奖励档位答对题数")
    private Integer rewardCorrectCount;

    @Schema(description = "奖品类型")
    private Integer prizeType;

    @Schema(description = "奖品ID")
    private Long prizeId;

    @Schema(description = "奖品名称")
    private String prizeName;

    @Schema(description = "奖品图片")
    private String prizeImgUrl;

    @Schema(description = "奖品价值")
    private BigDecimal prizeValue;

    @Schema(description = "红包领取状态")
    private Integer claimStatus;

    @Schema(description = "红包调起支付参数")
    private String packageInfo;

    @Schema(description = "红包商户转账单号")
    private String outBillNo;

    @Schema(description = "结果文案")
    private String message;
}

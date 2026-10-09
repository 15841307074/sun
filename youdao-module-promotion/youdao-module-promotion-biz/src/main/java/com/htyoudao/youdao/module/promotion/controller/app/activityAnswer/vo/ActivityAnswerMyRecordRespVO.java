package com.htyoudao.youdao.module.promotion.controller.app.activityAnswer.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 有奖问答我的答题记录响应。
 */
@Data
public class ActivityAnswerMyRecordRespVO {

    @Schema(description = "答题记录ID")
    private Long recordId;

    @Schema(description = "答题编号")
    private String answerNo;

    @Schema(description = "活动ID")
    private Long activityId;

    @Schema(description = "活动标题")
    private String answerTitle;

    @Schema(description = "门店ID")
    private Long storeId;

    @Schema(description = "门店名称")
    private String storeName;

    @Schema(description = "题目数量")
    private Integer questionCount;

    @Schema(description = "正确数量")
    private Integer correctCount;

    @Schema(description = "错误数量")
    private Integer wrongCount;

    @Schema(description = "正确率")
    private BigDecimal accuracy;

    @Schema(description = "答题状态")
    private Integer status;

    @Schema(description = "取消状态 0未取消 1已取消")
    private Integer cancelStatus;

    @Schema(description = "开始时间")
    private LocalDateTime startTime;

    @Schema(description = "参与时间，与开始答题时间一致")
    private LocalDateTime participationTime;

    @Schema(description = "提交时间")
    private LocalDateTime submitTime;

    @Schema(description = "每题对错明细")
    private List<ActivityAnswerSubmitDetailRespVO> details;
}

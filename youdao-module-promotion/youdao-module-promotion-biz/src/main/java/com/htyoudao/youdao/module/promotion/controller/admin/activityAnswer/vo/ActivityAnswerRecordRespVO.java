package com.htyoudao.youdao.module.promotion.controller.admin.activityAnswer.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ActivityAnswerRecordRespVO {

    private Long id;
    private String answerNo;
    private Long activityId;
    private Long memberId;
    private Long memberMobile;
    private String memberName;
    private Long storeId;
    private String storeName;
    private Integer questionCount;
    private Integer correctCount;
    private Integer wrongCount;
    private BigDecimal accuracy;
    private Integer status;
    private Integer cancelStatus;
    private LocalDateTime startTime;
    private LocalDateTime participationTime;
    private LocalDateTime activityTime;
    private LocalDateTime submitTime;
}

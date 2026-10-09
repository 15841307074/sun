package com.htyoudao.youdao.module.promotion.controller.admin.activityAnswer.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ActivityAnswerRewardLogRespVO {

    private Long id;
    private Long recordId;
    private String answerNo;
    private Long activityId;
    private Long memberId;
    private Long memberMobile;
    private String memberName;
    private Long storeId;
    private String storeName;
    private Long rewardId;
    private Integer rewardCorrectCount;
    private Integer prizeType;
    private Long prizeId;
    private String prizeName;
    private String prizeImgUrl;
    private BigDecimal prizeValue;
    private Integer claimStatus;
    private String packageInfo;
    private String outBillNo;
    private Integer prizeState;
    private String receiveUser;
    private String receiveMobile;
    private String receiveAddress;
    private String expressCompany;
    private String trackingNumber;
    private LocalDateTime grantTime;
}

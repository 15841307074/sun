package com.htyoudao.youdao.module.promotion.dal.dataobject.activitySign;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.htyoudao.youdao.framework.mybatis.core.dataobject.BusinessBaseDO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
public class ActivitySignRewardRecordDO extends BusinessBaseDO {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long activityId;
    private Long prizeId;
    private String memberMobile;
    private Integer mobileShard;
    private Long memberId;
    private String memberName;
    private Integer gender;
    private Integer memberCategory;
    private Long storeId;
    private String storeName;
    private String periodKey;
    private LocalDateTime periodStartTime;
    private LocalDateTime periodEndTime;
    private Integer resetEnabledSnapshot;
    private Integer resetTypeSnapshot;
    private String resetDaysSnapshot;
    private String rewardRuleSnapshot;
    private Integer prizeType;
    private String prizeContent;
    private String prizeImgUrl;
    private BigDecimal prizeValue;
    private Integer signRuleType;
    private Integer triggerDays;
    private LocalDate signDate;
    private Long signRecordId;
    private String grantKey;
    private Integer issueStatus;
    private LocalDateTime issueTime;
    private String failReason;
    private String externalRecordId;
    private Integer prizeState;
    private String receiveUser;
    private String receiveMobile;
    private String receiveAddress;
    private String trackingNumber;
    private String expressCompany;
    private String outBillNo;
    private Integer claimStatus;
    private String packageInfo;
}

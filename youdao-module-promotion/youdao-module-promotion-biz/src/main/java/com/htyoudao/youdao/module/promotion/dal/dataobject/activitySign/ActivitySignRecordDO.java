package com.htyoudao.youdao.module.promotion.dal.dataobject.activitySign;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.htyoudao.youdao.framework.mybatis.core.dataobject.BusinessBaseDO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
public class ActivitySignRecordDO extends BusinessBaseDO {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long activityId;
    private String memberMobile;
    private Integer mobileShard;
    private Long triggerMemberId;
    private String memberName;
    private Long storeId;
    private String storeName;
    private String periodKey;
    private LocalDateTime periodStartTime;
    private LocalDateTime periodEndTime;
    private Integer resetEnabledSnapshot;
    private Integer resetTypeSnapshot;
    private String resetDaysSnapshot;
    private LocalDate signDate;
    private LocalDateTime signTime;
    private Integer signType;
    private Integer continuousDaysAfter;
    private Integer totalDaysAfter;
}

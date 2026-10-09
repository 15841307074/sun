package com.htyoudao.youdao.module.promotion.dal.dataobject.activitySign;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.htyoudao.youdao.framework.mybatis.core.dataobject.BusinessBaseDO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "activity_sign_reward", autoResultMap = true)
public class ActivitySignRewardDO extends BusinessBaseDO {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long activityId;
    private Integer prizeType;
    private Long prizeId;
    private String prizeContent;
    private String prizeImgUrl;
    private BigDecimal prizeValue;
    private Integer signRuleType;
    private Integer signDays;
    private Integer grantMode;
}

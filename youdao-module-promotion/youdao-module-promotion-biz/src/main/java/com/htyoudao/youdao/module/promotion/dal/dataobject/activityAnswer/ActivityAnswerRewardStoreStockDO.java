package com.htyoudao.youdao.module.promotion.dal.dataobject.activityAnswer;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.htyoudao.youdao.framework.mybatis.core.dataobject.BusinessBaseDO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@TableName("activity_answer_reward_store_stock")
@EqualsAndHashCode(callSuper = true)
public class ActivityAnswerRewardStoreStockDO extends BusinessBaseDO {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    @Schema(description = "活动主表ID")
    private Long activityId;

    @Schema(description = "奖励档位ID")
    private Long rewardId;

    @Schema(description = "门店ID")
    private Long storeId;

    @Schema(description = "门店奖励总库存，0不限")
    private Integer totalNum;

    @Schema(description = "门店奖励已用库存")
    private Integer usedNum;
}

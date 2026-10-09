package com.htyoudao.youdao.module.promotion.dal.dataobject.activityVote;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 投票奖励删除记录表（物理删除前备份）
 */
@TableName(value = "activity_vote_reward_delete_log", autoResultMap = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActivityVoteRewardDeleteLogDO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 原奖励ID */
    private Long rewardId;
    /** 活动主表ID */
    private Long activityId;
    /** 奖品类型 1积分 2优惠券 3优惠券包 4实物奖品 5现金红包 */
    private Integer prizeType;
    /** 奖品id */
    private Long prizeId;
    /** 优惠券/包名称 */
    private String couponName;
    /** 奖品名称快照 */
    private String prizeName;
    /** 奖品图片快照 */
    private String prizeImgUrl;
    /** 奖品价值快照 */
    private BigDecimal prizeValue;
    /** 总库存 */
    private Integer totalNum;
    /** 已发库存 */
    private Integer usedNum;
    /** 删除时间 */
    private LocalDateTime deleteTime;
    /** 操作人 */
    private String operator;
    /** 项目ID */
    private Long businessId;
}

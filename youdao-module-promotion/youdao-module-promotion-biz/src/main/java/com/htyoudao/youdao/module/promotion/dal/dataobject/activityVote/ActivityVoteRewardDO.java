package com.htyoudao.youdao.module.promotion.dal.dataobject.activityVote;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.htyoudao.youdao.framework.mybatis.core.dataobject.BusinessBaseDO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.math.BigDecimal;

@TableName(value = "activity_vote_reward", autoResultMap = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActivityVoteRewardDO extends BusinessBaseDO {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

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
    /** 总库存 0不限库存 */
    private Integer totalNum;
    /** 已发库存 */
    private Integer usedNum;
}

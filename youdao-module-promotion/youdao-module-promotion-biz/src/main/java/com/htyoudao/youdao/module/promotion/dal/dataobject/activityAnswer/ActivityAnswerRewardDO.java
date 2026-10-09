package com.htyoudao.youdao.module.promotion.dal.dataobject.activityAnswer;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.htyoudao.youdao.framework.mybatis.core.dataobject.BusinessBaseDO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@TableName("activity_answer_reward")
@EqualsAndHashCode(callSuper = true)
public class ActivityAnswerRewardDO extends BusinessBaseDO {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    @Schema(description = "活动主表 ID")
    private Long activityId;

    @Schema(description = "答对题数档位")
    private Integer correctCount;

    @Schema(description = "奖品类型 1积分 2优惠券 3优惠券包 4实物 5现金红包")
    private Integer prizeType;

    @Schema(description = "奖品id(根据类型判断是优惠券 id 还是商品 id)")
    private Long awardId;

    @Schema(description = "编码")
    private String code;

    @Schema(description = "优惠券名称")
    private String couponName;

    @Schema(description = "奖品名称")
    private String prizeName;

    @Schema(description = "奖品图片")
    private String prizeImgUrl;

    @Schema(description = "奖品价值")
    private BigDecimal prizeValue;

    @Schema(description = "奖品总库存，0 不限")
    private Integer totalNum;

    @Schema(description = "已发库存")
    private Integer usedNum;
}

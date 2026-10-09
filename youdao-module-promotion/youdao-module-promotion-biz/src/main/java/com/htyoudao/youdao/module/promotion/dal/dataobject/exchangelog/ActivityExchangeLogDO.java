package com.htyoudao.youdao.module.promotion.dal.dataobject.exchangelog;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.htyoudao.youdao.framework.mybatis.core.dataobject.BusinessBaseDO;
import lombok.*;

/**
 * 活动的兑换记录 DO
 *
 * @author dht
 */
@TableName("activity_exchange_log")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActivityExchangeLogDO extends BusinessBaseDO {

    /**
     * id
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long memberId;

    /**
     * 会员昵称
     */
    private String memberNickName;
    /**
     * 联系电话
     */
    private String memberMobile;
    /**
     * 活动id
     */
    private Long activityId;
    /**
     * 奖品类型 1 优惠券 2券包
     */
    private Integer awardType;
    /**
     * 奖品名称
     */
    private String awardName;
    /**
     * 奖品图片
     */
    private String awardPic;

    /**
     * 外键
     */
    private Long foreignId;

    /**
     * 兑换门店id
     */
    private Long storeId;

    /**
     * 发放形式
     */
    private Integer distributeMode;

    /**
     * 用户类型
     */
    private Integer userRestrictions;
}
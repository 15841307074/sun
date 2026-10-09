package com.htyoudao.youdao.module.promotion.dal.dataobject.activityJk;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.htyoudao.youdao.framework.mybatis.core.dataobject.BusinessBaseDO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.math.BigDecimal;


@TableName(value = "activity_jk_prize", autoResultMap = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActivityJkPrizeDO extends BusinessBaseDO {

    @Serial
    private static final long serialVersionUID = -5291058844334477585L;

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 活动主id
     */
    private Long activityId;


    /**
     * 奖品类型 1 积分 2 优惠卷 3 优惠卷包  4 实物 5现金红包
     */
    private Integer prizeType;



    /**
     * 奖品名称
     */
    private String prizeName;



    /**
     * 奖品数量
     */
    private Integer prizeNum;


    /**
     * 奖品价值
     */
    private BigDecimal prizeValue;


    /**
     * 奖品图片
     */
    private String prizeImgUrl;

    /**
     * 奖品id(根据类型判断是优惠卷 id 还是商品 id)
     */
    private Long awardId;


    /**
     * 已兑换数量
     */
    private Integer remainNum;

    /**
     * 优惠卷或者卷包名称
     */
    private String couponName;



    /**
     * 兑换条件（1 套系卡兑换 2 隐藏卡兑换）
     */
    private Integer exchangeConditions;



    /**
     * 兑换限制 （0 不限制  1 限制）
     */
    private Integer exchangeRestrictions;



    /**
     * 限制数量
     */
    private Integer exchangeCount;








}

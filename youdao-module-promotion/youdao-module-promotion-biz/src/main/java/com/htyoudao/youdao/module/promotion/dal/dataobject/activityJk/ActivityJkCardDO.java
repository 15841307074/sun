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


@TableName(value = "activity_jk_card", autoResultMap = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActivityJkCardDO extends BusinessBaseDO {

    @Serial
    private static final long serialVersionUID = -5291058844334477585L;

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 活动主id
     */
    private Long activityId;


    /**
     * 卡片类型 1 兜底卡 2 套系卡 3 万能卡 4 隐藏卡
     */
    private Integer cardType;



    /**
     * 卡片名称
     */
    private String cardName;



    /**
     * 卡片数量
     */
    private Integer cardNum;


    /**
     * 是否多次获取  0 不允许
     */
    private Integer isRepeat;


    /**
     * 多次获取次数
     */
    private Integer repeatCount;

    /**
     * 获取概率   单位%
     */
    private BigDecimal probability;

    /**
     * 卡片图片
     */
    private String cardImgUrl;


    /**
     * 已领取数量
     */
    private Integer remainNum;

    /**
     * 是否为保底卡片（0为普通卡 1 为兜底卡）
     */
    private Integer isGuarantees;



    /**
     * 保底次数
     */
    private Integer minimumNumber;



    /**
     * 卡片排序
     */
    private Integer cardSort;












}

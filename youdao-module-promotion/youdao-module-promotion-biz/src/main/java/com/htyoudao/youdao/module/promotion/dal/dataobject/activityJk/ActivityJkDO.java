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

/**
 * @author Yangqinglin
 * 营销活动表 -集卡活动
 */
@TableName(value = "activity_jk", autoResultMap = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActivityJkDO extends BusinessBaseDO {

    @Serial
    private static final long serialVersionUID = -7070525505627059503L;
    /**
     * 主键
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 活动主id
     */
    private Long activityId;

    /**
     * 活动封面图
     */
    private String activityCoverImage;


    /**
     * 活动背景图
     */
    private String activityBackgroundImage;

    /**
     * 我的奖励背景图
     */
    private String myBackgroundImage;


    /**
     * 抽卡按钮背景图
     */
    private String cardButtonBackgroundImage;


    /**
     * 任务按钮背景图
     */
    private String taskButtonBackgroundImage;


    /**
     * 活动详情长图
     */
    private String activityDetailsImage;

    /**
     * 按钮背景色
     */
    private String buttonBackgroundColor;


    /** 分享标题 */
    private String shareTitle;

    /** 分享内容 */
    private String shareNote;

    /** 分享图片 */
    private String shareImgUrl;

    /**
     * 每日签到（0代表不开启 1开启）
     */
    private Integer dailyAttendance;

    /**
     * 签到数
     */
    private Integer dailyAttenCount;

    /**
     * 下单获得（0关闭  1开启）
     */
    private Integer placeOrderStatus;


    /**
     * 下单类型（1 按品类限制  2 按商品限制）
     */
    private Integer placeOrderType;

    /**
     * 参与品类（1 全部 2 仅单品  3仅套餐）
     */
    private Integer categoryType;


    /**
     * 下单商品（1 全部商品 2 指定商品）
     */
    private Integer placeOrderProduct;

    /**
     * 支付门槛（0代表不限制）
     */
    private Integer paymentThreshold;


    /**
     * 支付金额
     */
    private BigDecimal paymentCount;


    /**
     * 下单集卡状态 0 每人每天  1不限制
     */
    @Schema(description = "下单集卡状态 0 每人每天  1不限制")
    private Integer placeOrderCard;

    /**
     * 下单集卡次数
     */
    private Integer placeOrderCardNumber;


    /**
     * 分享活动 （0代表不开启 1开启）
     */
    private Integer shareEvent;

    /**
     * 分享集卡次数
     */
    private Integer shareCount;


    /**
     * 奖励发放（1当日发放 2次日发放）
     */
    private Integer awardDistribution;


    /**
     * 1 不允许转发至好友  2 允许转发至好友 3 允许复制链接到好友
     */
    private Integer shareType;


    /**
     * 公共展示（0开启  1 关闭）
     */
    private Integer publicButton;

    /**
     * 浏览类型（0关闭 1开启）
     */
    private Integer browseType;


    /**
     * 浏览抽奖次数
     */
    private Integer browseCount;


    /**
     * 免费获得（0关闭 1开启）
     */
    private Integer freeStatus;

    /**
     * 免费限制次数
     */
    private Integer freeCount;



}
package com.htyoudao.youdao.module.promotion.dal.dataobject.activityCq;

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
import java.time.LocalDateTime;

@TableName(value = "activity_cq", autoResultMap = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActivityCqDO extends BusinessBaseDO {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 活动id
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
     * 活动详情图
     */
    private String activityDetailsImage;

    /**
     * 按钮背景色
     */
    private String buttonBackgroundColor;

    /**
     * 分享标题
     */
    private String shareTitle;

    /**
     * 分享内容
     */
    private String shareNote;

    /**
     * 分享图片
     */
    private String shareImgUrl;

    /**
     * 每日签到开关
     */
    private Integer dailyAttendance;

    /**
     * 签到获得次数
     */
    private Integer dailyAttenCount;

    /**
     * 免费集签开关
     */
    private Integer freeEvent;

    /**
     * 免费抽签次数
     */
    private Integer freeCount;

    /**
     * 下单获得开关
     */
    private Integer placeOrderStatus;

    /**
     * 下单类型
     */
    private Integer placeOrderType;

    /**
     * 参与品类
     */
    private Integer categoryType;

    /**
     * 下单商品范围
     */
    private Integer placeOrderProduct;

    /**
     * 支付门槛类型
     */
    private Integer paymentThreshold;

    /**
     * 支付门槛金额
     */
    private BigDecimal paymentCount;

    /**
     * 下单抽签码类型
     */
    private Integer placeOrderCode;

    /**
     * 下单抽签码次数
     */
    private Integer placeOrderCodeNumber;

    /**
     * 分享活动开关
     */
    private Integer shareEvent;

    /**
     * 分享获得次数
     */
    private Integer shareCount;

    /**
     * 浏览首页集签开关
     */
    private Integer browseHomeEvent;

    /**
     * 浏览首页每人每天上限获得次数
     */
    private Integer browseHomeCount;

    /**
     * 参与开始时间
     */
    private LocalDateTime startDateTime;

    /**
     * 参与结束时间
     */
    private LocalDateTime endDateTime;

    /**
     * 结果公布时间
     */
    private LocalDateTime resultPublishTime;

    /**
     * 开奖状态 0未开奖 1开奖中 2已开奖
     */
    private Integer drawStatus;

    /**
     * 公开展示开关
     */
    private Integer publicButton;
    /**
     * 1 不允许转发至好友  2 允许转发至好友 3 允许复制链接到好友
     */
    private Integer shareType;

}

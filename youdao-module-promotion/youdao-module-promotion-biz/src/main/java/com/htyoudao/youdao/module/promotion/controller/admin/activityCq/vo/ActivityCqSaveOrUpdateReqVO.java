package com.htyoudao.youdao.module.promotion.controller.admin.activityCq.vo;

import cn.hutool.core.date.DateTime;
import com.alibaba.fastjson2.annotation.JSONField;
import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Data
public class ActivityCqSaveOrUpdateReqVO {

    /**
     * 子表id
     */

    @Schema(description = "子表id")
    private Long id;

    /**
     * 活动id
     */

    @Schema(description = "活动id")
    private Long activityId;

    /**
     * 活动类型
     */

    @Schema(description = "活动类型")
    private Integer activityType;

    /**
     * 活动名称
     */

    @Schema(description = "活动名称")
    @NotBlank(message = "活动名称不能为空")
    private String activityName;

    /**
     * 是否启用
     */

    @Schema(description = "是否启用")
    private Integer isEnabled;

    /**
     * 活动封面图
     */

    @Schema(description = "活动封面图")
    private String activityCoverImage;

    /**
     * 活动背景图
     */

    @Schema(description = "活动背景图")
    private String activityBackgroundImage;

    /**
     * 活动详情图
     */

    @Schema(description = "活动详情图")
    private String activityDetailsImage;

    /**
     * 按钮背景色
     */

    @Schema(description = "按钮背景色")
    private String buttonBackgroundColor;

    /**
     * 分享标题
     */

    @Schema(description = "分享标题")
    private String shareTitle;

    /**
     * 分享内容
     */

    @Schema(description = "分享内容")
    private String shareNote;

    /**
     * 分享图片
     */

    @Schema(description = "分享图片")
    private String shareImgUrl;

    /**
     * 每日签到开关
     */

    @Schema(description = "每日签到开关")
    private Integer dailyAttendance;

    /**
     * 签到获得次数
     */

    @Schema(description = "签到获得次数")
    private Integer dailyAttenCount;

    /**
     * 免费集签开关
     */

    @Schema(description = "免费集签开关")
    private Integer freeEvent;

    /**
     * 免费抽签次数
     */

    @Schema(description = "免费抽签次数")
    private Integer freeCount;

    /**
     * 下单获得开关
     */

    @Schema(description = "下单获得开关")
    private Integer placeOrderStatus;

    /**
     * 下单类型
     */

    @Schema(description = "下单类型")
    private Integer placeOrderType;

    /**
     * 参与品类
     */

    @Schema(description = "参与品类")
    private Integer categoryType;

    /**
     * 下单商品范围
     */

    @Schema(description = "下单商品范围")
    private Integer placeOrderProduct;

    /**
     * 支付门槛类型
     */

    @Schema(description = "支付门槛类型")
    private Integer paymentThreshold;

    /**
     * 支付门槛金额
     */

    @Schema(description = "支付门槛金额")
    private BigDecimal paymentCount;

    /**
     * 下单抽签码类型
     */

    @Schema(description = "下单抽签码类型")
    private Integer placeOrderCode;

    /**
     * 下单抽签码次数
     */

    @Schema(description = "下单抽签码次数")
    private Integer placeOrderCodeNumber;

    /**
     * 分享活动开关
     */

    @Schema(description = "分享活动开关")
    private Integer shareEvent;

    /**
     * 分享获得次数
     */

    @Schema(description = "分享获得次数")
    private Integer shareCount;

    /**
     * 浏览首页集签开关
     */

    @Schema(description = "浏览首页集签开关")
    private Integer browseHomeEvent;

    /**
     * 浏览首页每人每天上限获得次数
     */

    @Schema(description = "浏览首页每人每天上限获得次数")
    private Integer browseHomeCount;

    /**
     * 结果公布时间
     */

    @Schema(description = "结果公布时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm", timezone = "GMT+8")
    private Date resultPublishTime;

    // 替换这两个字段
    @Schema(description = "开始日期")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm", timezone = "GMT+8")
    private Date startDate;

    @Schema(description = "结束日期")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm", timezone = "GMT+8")
    private Date endDate;
  // 替换 Hutool DateTime
    /**
     * 开奖状态 0未开奖 1开奖中 2已开奖
     */

    @Schema(description = "开奖状态")
    private Integer drawStatus;

    /**
     * 公开展示
     */

    @Schema(description = "公开展示")
    private Integer publicButton;

    /**
     * 活动备注
     */

    @Schema(description = "活动备注")
    private String activityRemark;

    /**
     * 活动规则
     */

    @Schema(description = "活动规则")
    private String activityRules;

    /**
     * 活动门店
     */

    @Schema(description = "活动门店")
    @NotNull(message = "活动门店不能为空")
    private Integer activityStore;


    /**
     * 指定日期
     */

    @Schema(description = "指定日期")
    private List<Integer> dayNumberList;

    /**
     * 指定星期
     */

    @Schema(description = "指定星期")
    private List<Integer> weekNumberList;

    /**
     * 指定时间段
     */

    @Schema(description = "指定时间段")
    private List<String> timeRangeList;

    /**
     * 门店id集合
     */

    @Schema(description = "门店id集合")
    private List<Long> storeIds;

    /**
     * 商品id集合
     */

    @Schema(description = "商品id集合")
    private List<Long> commodityIds;

    /**
     * 社群专享
     */

    @Schema(description = "社群专享")
    private Integer communityFlag;

    /**
     * 引导图片
     */

    @Schema(description = "引导图片")
    private String guideImage;

    /**
     * 奖品设置
     */

    @Schema(description = "奖品设置")
    @NotEmpty(message = "奖品设置不能为空")
    private List<ActivityCqPrizeReqVO> prizeReqVOList;
    @Schema(description = "1 不允许转发至好友  2 允许转发至好友 3 允许复制链接到好友")
    private Integer shareType;
}

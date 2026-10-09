package com.htyoudao.youdao.module.promotion.dal.dataobject.activityAnswer;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.htyoudao.youdao.framework.mybatis.core.dataobject.BusinessBaseDO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@TableName("activity_answer")
@EqualsAndHashCode(callSuper = true)
public class ActivityAnswerDO extends BusinessBaseDO {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    @Schema(description = "活动主表ID")
    private Long activityId;

    @Schema(description = "状态 0禁用 1启用")
    @TableField(exist = false)
    private Integer state;

    @Schema(description = "活动门店范围 0指定门店 1全部门店")
    private Integer activityStore;

    @Schema(description = "活动标题")
    @TableField(exist = false)
    private String answerTitle;

    @Schema(description = "活动规则")
    @TableField(exist = false)
    private String answerRule;

    @Schema(description = "活动封面图")
    private String activityImgUrl;

    @Schema(description = "活动背景图")
    private String activityBackground;

    @Schema(description = "开始答题按钮图")
    private String buttonImgUrl;

    @Schema(description = "活动详情长图，可不传")
    private String activityDetailLongImage;

    @Schema(description = "答题页面背景图")
    private String questionBackgroundImage;

    @Schema(description = "未选择选项图")
    private String unselectedOptionImage;

    @Schema(description = "已选择选项图")
    private String selectedOptionImage;

    @Schema(description = "上一题按钮图")
    private String previousButtonImage;

    @Schema(description = "下一题按钮图")
    private String nextButtonImage;

    @Schema(description = "确认提交按钮图")
    private String submitButtonImage;

    @Schema(description = "背景颜色")
    private String backgroundColor;

    @Schema(description = "分享标题")
    private String shareTitle;

    @Schema(description = "分享内容")
    private String shareNote;

    @Schema(description = "分享图片")
    private String shareImgUrl;

    @Schema(description = "分享类型 1不允许转发 2允许转发 3允许复制链接")
    private Integer shareType;

    @Schema(description = "公共展示 0开启 1关闭")
    private Integer publicButton;

    @Schema(description = "社群专享 1不开启 2开启")
    private Integer communityFlag;

    @Schema(description = "引导图片")
    private String guideImage;

    @Schema(description = "答题总次数 0不限制")
    private Integer answerTotalNumber;

    @Schema(description = "次数计算规则 1按天 2按场次")
    private Integer calculationRules;

    @Schema(description = "门店奖励库存规则 1共用库存 2独立库存")
    private Integer prizePoolRules;

    @Schema(description = "奖励及答题规则重置 0关闭 1开启")
    private Integer answerRulesReset;

    @Schema(description = "免费获得 0关闭 1开启")
    private Integer freeStatus;

    @Schema(description = "免费答题次数")
    private Integer freeCount;

    @Schema(description = "每日签到 0关闭 1开启")
    private Integer signStatus;

    @Schema(description = "签到答题次数")
    private Integer signCount;

    @Schema(description = "积分获得 0关闭 1开启")
    private Integer pointsStatus;

    @Schema(description = "积分任务次数规则 0每人每天 1不限制")
    private Integer pointsType;

    @Schema(description = "积分任务限制次数")
    private Integer pointsCount;

    @Schema(description = "下单获得 0关闭 1开启")
    private Integer orderStatus;

    @Schema(description = "下单类型 1按品类限制 2按商品限制")
    private Integer placeOrderType;

    @Schema(description = "参与品类 1全部 2仅单品 3仅套餐")
    private Integer categoryType;

    @Schema(description = "下单商品 1全部商品 2指定商品")
    private Integer placeOrderProduct;

    @Schema(description = "支付门槛 0不限制 1限制")
    private Integer paymentType;

    @Schema(description = "支付金额")
    private BigDecimal paymentCount;

    @Schema(description = "下单任务次数规则 0每人每天 1不限制")
    private Integer placeOrderAnswer;

    @Schema(description = "下单可获得答题次数")
    private Integer placeOrderAnswerNumber;

    @Schema(description = "分享活动 0关闭 1开启")
    private Integer shareEvent;

    @Schema(description = "分享可获得答题次数")
    private Integer shareCount;

    @Schema(description = "浏览首页 0关闭 1开启")
    private Integer browseType;

    @Schema(description = "浏览可获得答题次数")
    private Integer browseCount;

    @Schema(description = "创建人名称")
    private String createUserName;

    @Schema(description = "修改人名称")
    private String updateUserName;
}

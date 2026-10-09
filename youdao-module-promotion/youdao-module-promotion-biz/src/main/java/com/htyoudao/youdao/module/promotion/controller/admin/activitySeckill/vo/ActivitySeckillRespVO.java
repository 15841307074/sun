package com.htyoudao.youdao.module.promotion.controller.admin.activitySeckill.vo;

import com.htyoudao.youdao.module.promotion.api.activity.VO.ActivityChannelRespVO;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
@Schema(description = "管理后台 - 营销活动秒杀场次  返回 VO")
@Data
public class ActivitySeckillRespVO implements Serializable {


    @Serial
    private static final long serialVersionUID = -234465169090362986L;

    @Schema(description = "id")
    private Long id;

    /**
     * 活动名称
     */
    @Schema(description = "活动名称")

    private String activityName;

    /**
     * 活动类型
     */
    @Schema(description = "营销类型 （  1 n件n折，2秒杀活动）")
    private Integer activityType;

    @Schema(description = "类型 1优惠券 2或空 商品")
    private Integer discountType;


    /**
     * 是否优惠叠加（0-否，1-是）
     */
    @Schema(description = "优惠叠加（0不叠加 1叠加）")

    private Integer discountStackable;
    /**
     * 可叠加活动（存储活动标识，如1-优惠券）
     */
    @Schema(description = "1 优惠卷")
    private List<Integer> stackableActivitieList = new ArrayList<>();

    /**
     * 活动备注（最多200字）
     */
    @Schema(description = "活动备注")
    private String activityRemark;

    /**
     * 是否全门店参与（0-否，1-是）
     */
    @Schema(description = "是否全门店参与（0-否，1-是）")
    private Integer activityStore = 0;

    /**
     * 推送人群（1.所有用户 2.新用户 3.老用户 4.回归用户  5指定人群）
     */
    @Schema(description = "推送人群（1.所有用户 2.新用户 3.老用户 4.回归用户  5指定人群）")
    private Integer participantGroup;

    /**
     * 选择人群（存储人群ID，逗号分割）
     */
    @Schema(description = "选择人群（存储人群ID，逗号分割）")
    private List<Long> selectedGroupList = new ArrayList<>();
    /**
     * 选择人群返回集合
     */
    @Schema(description = "选择人群返回集合")
    private List<ActivitySeckillCrowdRespVO>  activitySeckillCrowdRespVOList = new ArrayList<>();

    /**
     * 活动规则（富文本内容）
     */
    @Schema(description = "活动规则（富文本内容）")
    private String activityRules;

    /**
     * 是否开启（0不开启 1开启）
     */
    private Integer isEnabled = 0;

    /**
     * 开始日期
     */
    @Schema(description = "开始日期）")
    private Date startDate;

    /**
     * 结束日期
     */
    @Schema(description = "结束日期")
    private Date endDate;

    @Schema(description = "日期集合")
    private List<Integer> dayNumberList = new ArrayList<>();

    @Schema(description = "周几集合")
    private List<Integer> weekNumberList = new ArrayList<>();

    @Schema(description = "门店ID集合")
    private List<Long> storeIds = new ArrayList<>();

    // 图片地址
    @Schema(description = "图片地址")
    @NotNull(message = "图片地址 不能为空")
    private String imageUrl;
    // 背景色（如#FFFFFF）
    @Schema(description = "背景色（如#FFFFFF）")
    @NotNull(message = "背景色 不能为空")
    private String backgroundColor;

    // 门店是否购买限制（0-否，1-是）
    @Schema(description = "门店是否购买限制（0-否，1-是）")
    private Integer isStoreLimit;

    // 每家店限购多少次
    @Schema(description = "每家店限购多少次")
    private Integer storeLimitCount;

    // 分享图片
    @Schema(description = "分享图片")
    private String shareImageUrl;

    // 分享标题
    @Schema(description = "分享标题")
    private String shareTitle;

    // 分享描述
    @Schema(description = "分享描述")
    private String shareDescription;


    // 分享设置（1-不允许转发至好友，2-允许转发至好友，3-允许复制链接至好友）
    @Schema(description = "分享设置（1-不允许转发至好友，2-允许转发至好友，3-允许复制链接至好友）")
    private Integer shareSetting;

    // 是否显示弹幕开关（0-关，1-开）
    @Schema(description = "是否显示弹幕开关（0-关，1-开）")
    private Integer isBarrageShow;

    @Schema(description = "秒杀商品集合")
    private List<ActivitySeckillCommodityRespVO>  activitySeckillCommodityRespVOS = new ArrayList<>();

    @Schema(description = "秒杀优惠券集合")
    private List<ActivitySeckillCouponRespVO>  activitySeckillCouponRespVOS = new ArrayList<>();


    @Schema(description = "秒杀场次集合")
    private List<ActivitySeckillTimeRespVO>   activitySeckillTimeRespVOS = new ArrayList<>();

    @Schema(description = "秒杀推广集合")
    List<ActivityChannelRespVO>  activityChannelRespVOList = new ArrayList<>();

    @Schema(description = "门店集合包含门店名")
    private List<ActivitySeckillStoreNameRespVO>   activitySeckillStoreNameRespVOS = new ArrayList<>();

    @Schema(description = "社群专享 1 不开启  2 开启")
    private Integer communityFlag;

    @Schema(description = "引导图片")
    private String guideImage;

}

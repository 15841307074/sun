package com.htyoudao.youdao.module.promotion.controller.admin.activitySeckill.vo;

import com.alibaba.fastjson2.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import lombok.Data;

@Schema(description = "管理后台 - 营销活动秒杀优惠券 新增/修改 Request VO")
@Data
public class ActivitySeckillCouponSaveVO {

    @Schema(description = "id")
    private Long id;

    /**
     * 活动名称
     */
    @Schema(description = "活动名称")
    @NotNull(message = "活动名称 不能为空")
    private String activityName;

    /**
     * 活动类型
     */
    @Schema(description = "营销类型 （  1 n件n折，2秒杀活动）")
    @NotNull(message = "营销类型 （  1 n件n折，2秒杀活动）不能为空")
    private Integer activityType;

    /**
     * 是否优惠叠加（0-否，1-是）
     */
    @Schema(description = "优惠叠加（0不叠加 1叠加）")
    @NotNull(message = "优惠叠加（0不叠加 1叠加） 不能为空")
    private Integer discountStackable;
    /**
     * 可叠加活动（存储活动标识，如1-优惠券）
     */
    @Schema(description = "1 优惠卷")
    private List<Integer> stackableActivitieList;

    /**
     * 活动备注（最多200字）
     */
    @Schema(description = "活动备注")
    private String activityRemark;

    /**
     * 是否全门店参与（0-否，1-是）
     */
    @Schema(description = "是否全门店参与（0-否，1-是）")
    @NotNull(message = "是否全门店参与（0-否，1-是）不能为空")
    private Integer activityStore = 0;

    /**
     * 推送人群（1.所有用户 2.新用户 3.老用户 4.回归用户  5指定人群）
     */
    @Schema(description = "推送人群（1.所有用户 2.新用户 3.老用户 4.回归用户  5指定人群）")
    @NotNull(message = "推送人群（1.所有用户 2.新用户 3.老用户 4.回归用户  5指定人群）不能为空")
    private Integer participantGroup;

    /**
     * 选择人群（存储人群ID，逗号分割）
     */
    @Schema(description = "选择人群（存储人群ID，逗号分割）")
    private List<Long> selectedGroupList;

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
    @JsonFormat(pattern = "yyyy-MM-dd")
    @JSONField(format = "yyyy-MM-dd")
    private Date startDate;

    /**
     * 结束日期
     */
    @Schema(description = "结束日期")
    @JsonFormat(pattern = "yyyy-MM-dd")
    @JSONField(format = "yyyy-MM-dd")
    private Date endDate;

    @Schema(description = "日期集合")
    private List<Integer> dayNumberList;

    @Schema(description = "周几集合")
    private List<Integer> weekNumberList;
    @Schema(description = "门店ID集合")
    private List<Long> storeIds;

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
    @NotNull(message = "门店是否购买限制（0-否，1-是） 不能为空")
    private Integer isStoreLimit;
    // 每家店限购多少次
    @Schema(description = "每家店限购多少次")
    private Integer storeLimitCount;

    // 分享设置（1-不允许转发至好友，2-允许转发至好友，3-允许复制链接至好友）
    @Schema(description = "分享设置（1-不允许转发至好友，2-允许转发至好友，3-允许复制链接至好友）")
    @NotNull(message = "分享设置（1-不允许转发至好友，2-允许转发至好友，3-允许复制链接至好友） 不能为空")
    private Integer shareSetting;
    // 是否显示弹幕开关（0-关，1-开）
    @Schema(description = "是否显示弹幕开关（0-关，1-开）")
    @NotNull(message = "是否显示弹幕开关（0-关，1-开） 不能为空")
    private Integer isBarrageShow;

    @Schema(description = "秒杀场次")
    @NotEmpty(message = "秒杀场次 不能为空")
    @Valid
    private List<ActivitySeckillTimeSaveReqVO>  activitySeckillTimeSaveReqVOList = new ArrayList<>();

    @Schema(description = "秒杀商品")
    @NotEmpty(message = "秒杀商品 不能为空")
    @Valid
    private List<ActivitySeckillCommoditySaveReqVO>  activitySeckillCommoditySaveReqVOList = new ArrayList<>();

}

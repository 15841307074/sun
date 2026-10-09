package com.htyoudao.youdao.module.promotion.controller.admin.lottery.vo;

import com.alibaba.fastjson2.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 缓存vo
 */
@Schema(description = "管理后台 - 活动配置 Request VO")
@Data
public class LotterySettingsCacheVO {

    /**
     * id
     */
    @Schema(name = "id", description = "id")
    private Long id;

    /**
     * 抽奖类型 1 转盘 2 九宫格 3 福袋 4 盲盒
     */
    @Schema(name = "lotteryType", description = "抽奖类型 1 转盘 2 九宫格 3 福袋 4 盲盒")
    private Integer lotteryType;

    /**
     * 活动标题
     */
    @Schema(name = "lotteryTitle", description = "活动标题")
    @NotNull(message = "活动标题不能为空")
    private String lotteryTitle;


    /**
     * 活动图片
     */
    @Schema(name = "activityImgUrl", description = "活动封面图")
    @NotNull(message = "活动封面图不能为空")
    private String activityImgUrl;




    /**
     * 活动背景图
     */
    @Schema(name = "activityBackground", description = "活动背景图")
    @NotNull(message = "活动背景图不能为空")
    private String activityBackground;


    /**
     * 按钮抽奖图
     */
    @Schema(name = "buttonImgUrl", description = "按钮抽奖图")
    @NotNull(message = "按钮抽奖图不能为空")
    private String buttonImgUrl;




    /**
     * 状态 0 禁用 1 启用
     */
    @Schema(name = "state", description = "状态 0 禁用 1 启用")
    private Integer state;


    // 背景色（如#FFFFFF）
    @Schema(description = "背景色（如#FFFFFF）")
    @NotNull(message = "背景色不能为空")
    private String backgroundColor;


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


    /**
     * 活动类型
     */
    @Schema(description = "营销类型 （  1 n件n折，2秒杀活动 3.抽奖活动）")
    @NotNull(message = "营销类型 （  1 n件n折，2秒杀活动 3.抽奖活动）不能为空")
    private Integer activityType;

    @Schema(description = "日期集合")
    private List<Integer> dayNumberList;

    @Schema(description = "周几集合")
    private List<Integer> weekNumberList;


    @Schema(description = "时间段集合")
    private List<String> timeRangeList;



    /**
     * 活动备注（最多200字）
     */
    @Schema(description = "活动备注")
    private String activityRemark;


    @Schema(description = "抽奖方式 1 免费抽奖 2 积分抽奖 3 下单抽奖")
    @NotNull(message = "抽奖方式不能为空")
    private Integer lotteryMethod;


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
     * 抽奖次数 0 无限制 其余 （每天/次）
     */
    @Schema(name = "lotteryLimit", description = "抽奖次数 0 无限制 其余 （每天/次）")
    private Integer lotteryLimit;


    /**
     * 抽奖总次数 0 无限制 其余（总次数）
     */
    @Schema(name = "lotteryTotalNumber", description = "抽奖总次数 0 无限制 （总次数）")
    private Integer lotteryTotalNumber;



    @Schema(description = "门店ID集合")
    private List<Long> storeIds;

    /**
     * 奖品列表
     */
    @Schema(name = "prizes", description = "奖品列表")
    private List<LotteryPrizeReqVO> prizes = new ArrayList<>();


    /**
     * 活动规则（富文本内容）
     */
    @Schema(description = "活动规则（富文本内容）")
    private String activityRules;

    /**
     * 抽奖积分价格
     */
    @Schema(name = "price", description = "抽奖积分价格")
    private Integer price;

    /**
     * 分享标题
     */
    @Schema(name = "shareTitle", description = "分享标题")
    private String shareTitle;

    /**
     * 分享内容
     */
    @Schema(name = "shareNote", description = "分享内容")
    private String shareNote;

    /**
     * 分享图片
     */
    @Schema(name = "shareImgUrl", description = "分享图片")
    private String shareImgUrl;


    /**
     * 活动开始时间
     */
    @Schema(name = "lotteryStartTime", description = "活动开始时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @JSONField(format="yyyy-MM-dd HH:mm:ss")
    private Date lotteryStartTime;

    /**
     * 活动结束时间
     */
    @Schema(name = "lotteryEndTime", description = "活动结束时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @JSONField(format="yyyy-MM-dd HH:mm:ss")
    private Date lotteryEndTime;


    /**
     * 门店 id
     */
    @Schema(name = "storeId", description = "门店 id")
    private Long storeId;



    /** 是否是免费 */
    @Schema(name = "isFree", description = "是否是免费")
    private Integer isFree;

    /**
     * 是否开启（0不开启 1开启）
     */
    private Integer isEnabled = 0;


    @Schema(description = "商品ID集合")
    private List<Long> commodityIds;









}

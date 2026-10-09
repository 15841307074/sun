package com.htyoudao.youdao.module.promotion.controller.admin.lottery.vo;

import com.alibaba.fastjson2.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.mzt.logapi.starter.annotation.DiffLogField;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.htyoudao.youdao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY;
import static com.htyoudao.youdao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

/**
 * pc接取 vo
 */
@Schema(description = "管理后台 - 活动配置 Request VO")
@Data
public class LotterySettingsReqVO {

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
     * 状态 0 禁用 1 启用
     */
    @Schema(name = "state", description = "状态 0 禁用 1 启用")
    private Integer state;

    @Schema(description = "上下架状态是否上架(0不开启 1开启)")
    @NotNull(message = "状态不能为空")
    private Integer isEnabled;


    /**
     * 活动标题
     */
    @Schema(name = "lotteryTitle", description = "活动标题")
    private String lotteryTitle;

    /**
     * 活动规则
     */
    @Schema(name = "lotteryRule", description = "活动规则")
    private String lotteryRule;

    /**
     * 活动开始时间
     */
    @Schema(name = "lotteryStartTime", description = "活动开始时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime lotteryStartTime;

    /**
     * 活动结束时间
     */
    @Schema(name = "lotteryEndTime", description = "活动结束时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime lotteryEndTime;

    /**
     * 抽奖次数 0 无限制 其余 （每天/次）
     */
    @Schema(name = "lotteryLimit", description = "抽奖次数 0 无限制 其余 （每天/次）")
    @DiffLogField(name = "抽奖次数")
    private Integer lotteryLimit;

    /**
     * 抽奖积分价格
     */
    @Schema(name = "price", description = "抽奖积分价格")
    @DiffLogField(name = "抽奖积分价格")
    private Integer price;

    /**
     * 分享标题
     */
    @Schema(name = "shareTitle", description = "分享标题")
    @DiffLogField(name = "分享标题")
    private String shareTitle;

    /**
     * 分享内容
     */
    @Schema(name = "shareNote", description = "分享内容")
    @DiffLogField(name = "分享内容")
    private String shareNote;

    /**
     * 分享图片
     */
    @Schema(name = "shareImgUrl", description = "分享图片")
    @DiffLogField(name = "分享图片")
    private String shareImgUrl;

    /**
     * 门店 id
     */
    @Schema(name = "storeId", description = "门店 id")
    @DiffLogField(name = "门店ID")
    private Long storeId;

    /**
     * 奖品列表
     */
    @Schema(name = "prizes", description = "奖品列表")
    private List<LotteryPrizeReqVO> prizes = new ArrayList<>();
    /**
     * 活动图片
     */
    @Schema(name = "activityImgUrl", description = "活动图片")
    @DiffLogField(name = "活动图片")
    private String activityImgUrl;
    /** 是否是免费 */
    @Schema(name = "isFree", description = "是否是免费")
    @DiffLogField(name = "是否是免费")
    private Integer isFree;



}

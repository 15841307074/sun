package com.htyoudao.youdao.module.promotion.controller.admin.lottery.vo;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson2.annotation.JSONField;
import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.htyoudao.youdao.module.commodity.api.DTO.CommodityDTO;
import com.htyoudao.youdao.module.promotion.controller.admin.activitySeckill.vo.ActivitySeckillCrowdRespVO;
import com.htyoudao.youdao.module.system.api.store.dto.StoreInfoDTO;
import com.htyoudao.youdao.module.system.api.store.dto.TagValueDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

@Data
public class LotterySettingsDetailDataRespVO{
    @Schema(description = "应用范围：0 门店，1 标签")
    private Integer appScope = 0;
    @Schema(description = "适用标签，任意命中即可参加")
    private List<Long> tagIds = new ArrayList<>();
    @Schema(description = "适用标签名称及 ID")
    private List<TagValueDTO> tagInfoDTOS = new ArrayList<>();



    /**
     * id
     */
    @Schema(name = "id", description = "id")
    private Long id;


    @Schema(name = "activityId", description = "id")
    private Long activityId;

    /**
     * 抽奖类型 1 转盘 2 九宫格 3 福袋 4 盲盒
     */
    @Schema(name = "lotteryType", description = "抽奖类型 1 转盘 2 九宫格 3 福袋 4 盲盒")
    private Integer lotteryType;

    /**
     * 活动标题
     */
    @Schema(name = "lotteryTitle", description = "活动标题")
    private String lotteryTitle;

    @Schema(name = "lotteryTitle", description = "活动标题")
    private String activityName;


    /**
     * 活动图片
     */
    @Schema(name = "activityImgUrl", description = "活动封面图")
    @NotNull(message = "活动封面图不能为空")
    private String activityImgUrl;


    @Schema(description = "支付门槛  0代表不限制")
    private BigDecimal paymentThreshold;




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
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    @JSONField(format = "yyyy-MM-dd")
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private Date startDate;

    /**
     * 结束日期
     */
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    @JSONField(format = "yyyy-MM-dd")
    @TableField(updateStrategy = FieldStrategy.IGNORED)
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

    @Schema(name = "shareType", description = "1 不允许转发至好友  2 允许转发至好友 3 允许复制链接到好友")
    private Integer shareType;


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



    /** 是否是免费 */
    @Schema(name = "isFree", description = "是否是免费")
    private Integer isFree;


    @Schema(name = "calculation_rules", description = "计算规则(1 按天计算抽奖次数  2 按场次计算抽奖次数)")
    private Integer calculationRules;

    @Schema(name = "prize_pool_rules", description = "门店奖池规则(1 共用奖池  2 独立奖池)")
    private Integer prizePoolRules;


    @Schema(name = "lottery_rules_reset", description = "奖品及抽奖规则重置（0关闭 1开启）")
    private Integer lotteryRulesReset;


    @Schema(name = "public_button", description = "公共展示（0开启  1 关闭）")
    private Integer publicButton;

    /**
     * 是否开启（0不开启 1开启）
     */
    private Integer isEnabled = 0;

    @Schema(description = "适用门店", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<StoreInfoDTO> storeInfoDTOS =new ArrayList<>();


    /**
     * 选择人群返回集合
     */
    @Schema(description = "选择人群返回集合")
    private List<ActivitySeckillCrowdRespVO>  activitySeckillCrowdRespVOList = new ArrayList<>();

    @Schema(description = "1 全部商品  2可选商品")
    private Integer activityProduct;

    @Schema(description = "适用商品", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<CommodityDTO> commodityDTOList = new ArrayList<>();



    @Schema(description = "指定时间段", requiredMode = Schema.RequiredMode.REQUIRED)
    private String timeRange;


    @Schema(description = "指定日期逗号分割", requiredMode = Schema.RequiredMode.REQUIRED)
    private String dayNumbers;


    @Schema(description = "指定周几逗号分割", requiredMode = Schema.RequiredMode.REQUIRED)
    private String weekNumbers;

    @Schema(description = "1 不开启  2 开启")
    private Integer communityFlag;

    @Schema(description = "引导图片")
    private String guideImage;

    @Schema(description = "浏览类型（0关闭 1开启）")
    private Integer browseType;

    @Schema(description = "浏览抽奖次数")
    private Integer browseCount;

    @Schema(description = "免费获得（0关闭 1开启")
    private Integer freeStatus;


    @Schema(description = "免费限制次数")
    private Integer freeCount;

    @Schema(description = "积分获得（0关闭 1开启）")
    private Integer pointsStatus;

    @Schema(description = "积分状态 0 每人每天 1不限制")
    private Integer pointsType;

    @Schema(description = "积分限制次数")
    private Integer pointsCount;

    @Schema(description = "下单获得（0关闭 1开启）")
    private Integer orderStatus;

    @Schema(description = "下单类型（1 按品类限制 2 按商品限制）")
    private Integer placeOrderType;

    @Schema(description = "参与品类（1 全部 2 仅单品 3仅套餐）")
    private Integer categoryType;
    @Schema(description = "支付门槛（0代表不限制 1限制）")
    private Integer paymentType;

    @Schema(description = "支付金额")
    private BigDecimal paymentCount;

    @Schema(description = "下单状态 0 每人每天 1不限制")
    private Integer placeOrderLottery;


    @Schema(description = "下单次数")
    private Integer placeOrderLotteryNumber;


    @Schema(description = "分享活动 （0代表不开启 1开启）")
    private Integer shareEvent;

    @Schema(description = "分享次数")
    private Integer shareCount;

    public void setDayNumberList(List<Integer> dayNumberList) {
        this.dayNumberList = dayNumberList;
        if(ObjectUtil.isNotEmpty(dayNumberList)){
            this.dayNumbers = dayNumberList.toString().replace("[", "").replace("]", "").replace(" ","");
        }else {
            this.dayNumbers = "";
        }
    }
    public void setWeekNumberList(List<Integer> weekNumberList) {
        this.weekNumberList = weekNumberList;
        if(ObjectUtil.isNotEmpty(weekNumberList)){
            this.weekNumbers = weekNumberList.toString().replace("[", "").replace("]", "").replace(" ","");
        }else {
            this.weekNumbers = "";
        }
    }
    public void setTimeRangeList(List<String> timeRangeList) {
        this.timeRangeList = timeRangeList;
        if(ObjectUtil.isNotEmpty(timeRangeList)){
            this.timeRange = timeRangeList.toString().replace("[", "").replace("]", "").replace(" ","");
        }else {
            this.timeRange = "";
        }
    }
    public void setDayNumbers(String dayNumbers) {
        this.dayNumbers = dayNumbers;
        if(ObjectUtil.isNotEmpty(dayNumbers)){
            String[] array = dayNumbers.split(",");
            this.dayNumberList = new ArrayList<>();
            for (String s : array) {
                this.dayNumberList.add(Integer.parseInt(s));
            }
        }else {
            this.dayNumberList = null;
        }
    }
    public void setWeekNumbers(String weekNumbers) {
        this.weekNumbers = weekNumbers;
        if(ObjectUtil.isNotEmpty(weekNumbers)){
            String[] array = weekNumbers.split(",");
            this.weekNumberList = new ArrayList<>();
            for (String s : array) {
                this.weekNumberList.add(Integer.parseInt(s));
            }
        }else {
            this.weekNumbers = null;
        }
    }
    public void setTimeRange(String timeRange) {
        this.timeRange = timeRange;
        if(ObjectUtil.isNotEmpty(timeRange)){
            String[] array = timeRange.split(",");
            this.timeRangeList = new ArrayList<>();
            Collections.addAll(this.timeRangeList, array);
        }else {
            this.timeRangeList = null;
        }
    }

}

package com.htyoudao.youdao.module.promotion.controller.admin.activityJk.vo;


import cn.hutool.core.date.DateTime;
import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson2.annotation.JSONField;
import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.htyoudao.youdao.module.commodity.api.DTO.CommodityDTO;
import com.htyoudao.youdao.module.promotion.controller.admin.activitySeckill.vo.ActivitySeckillCrowdRespVO;
import com.htyoudao.youdao.module.system.api.store.dto.StoreInfoDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * @author Yangqinglin
 */
@Schema(description = "管理后台 - 营销活动 集卡活动详情 Resp VO")
@Data
public class ActivityJkDetailRespVO {


    @Schema(description = "id")
    private Long id;

    @Schema(description = "活动类型 6 集卡")
    private Integer activityType;
    /**
     * 活动名称
     */
    @Schema(description = "活动名称")
    private String activityName;


    @Schema(description = "活动主id")
    private Long activityId;

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
     * 我的奖励背景图
     */
    @Schema(description = "我的奖励背景图")
    private String myBackgroundImage;


    /**
     * 抽卡按钮背景图
     */
    @Schema(description = "抽卡按钮背景图")
    private String cardButtonBackgroundImage;


    /**
     * 任务按钮背景图
     */
    @Schema(description = "任务按钮背景图")
    private String taskButtonBackgroundImage;


    /**
     * 活动详情长图
     */
    @Schema(description = "活动详情长图")
    private String activityDetailsImage;

    /**
     * 按钮背景色
     */
    @Schema(description = "按钮背景色")
    private String buttonBackgroundColor;

    /**
     * 每日签到（0代表不开启 1开启）
     */
    @Schema(description = "每日签到（0代表不开启 1开启）")
    private Integer dailyAttendance;

    /**
     * 签到数
     */
    @Schema(description = "签到数")
    private Integer dailyAttenCount;


    /**
     * 下单获得（0关闭  1开启）
     */
    @Schema(description = "下单获得（0关闭  1开启）")
    private Integer placeOrderStatus;


    /**
     * 下单类型（1 按品类限制  2 按商品限制）
     */
    @Schema(description = "下单类型（1 按品类限制  2 按商品限制）")
    private Integer placeOrderType;

    /**
     * 参与品类（1 全部 2 仅单品  3仅套餐）
     */
    @Schema(description = "参与品类（1 全部 2 仅单品  3仅套餐）")
    private Integer categoryType;


    /**
     * 下单商品（1 全部商品 2 指定商品）
     */
    @Schema(description = "下单商品（1 全部商品 2 指定商品）")
    private Integer placeOrderProduct;

    /**
     * 支付门槛（0代表不限制 1限制）
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
    @Schema(description = "下单集卡次数")
    private Integer placeOrderCardNumber;


    /**
     * 分享活动 （0代表不开启 1开启）
     */
    @Schema(description = "分享活动 （0代表不开启 1开启）")
    private Integer shareEvent;

    /**
     * 分享集卡次数
     */
    @Schema(description = "分享集卡次数")
    private Integer shareCount;


    /**
     * 奖励发放（1当日发放 2次日发放）
     */
    @Schema(description = "奖励发放（1当日发放 2次日发放）")
    private Integer awardDistribution;


    /**
     * 1 不允许转发至好友  2 允许转发至好友 3 允许复制链接到好友
     */
    @Schema(description = "1 不允许转发至好友  2 允许转发至好友 3 允许复制链接到好友")
    private Integer shareType;


    /**
     * 公共展示（0开启  1 关闭）
     */
    @Schema(description = "公共展示（0开启  1 关闭）")
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
     * 活动门店（1全部门店 2部分门店）
     */
    @Schema(description = "活动门店（0部分 1全部）")
    @NotNull(message = "活动门店 不能为空")
    private Integer activityStore;

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
    @Schema(description = "日期集合")
    private List<Integer> dayNumberList = new ArrayList<>();

    @Schema(description = "周几集合")
    private List<Integer> weekNumberList  = new ArrayList<>();

    @Schema(description = "时间段集合")
    private List<String> timeRangeList  = new ArrayList<>();


    @Schema(description = "门店ID集合")
    private List<Long> storeIds  = new ArrayList<>();

    @Schema(description = "商品ID集合")
    private List<Long> commodityIds  = new ArrayList<>();

    @Schema(description = "社群专享 1 不开启 2开启")
    private Integer communityFlag;

    @Schema(description = "奖品设置")
    @NotEmpty
    private List<ActivityJkPrizeReqVO> prizeReqVOList;

    @Schema(description = "卡片设置")
    @NotEmpty
    private List<ActivityJkCardReqVO> cardReqVOList;


    @Schema(description = "指定时间段", requiredMode = Schema.RequiredMode.REQUIRED)
    private String timeRange;


    @Schema(description = "指定日期逗号分割", requiredMode = Schema.RequiredMode.REQUIRED)
    private String dayNumbers;


    @Schema(description = "指定周几逗号分割", requiredMode = Schema.RequiredMode.REQUIRED)
    private String weekNumbers;

    @Schema(description = "适用门店", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<StoreInfoDTO> storeInfoDTOList =new ArrayList<>();


    @Schema(description = "适用商品", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<CommodityDTO> commodityDTOList = new ArrayList<>();

    @Schema(description = "引导图片")
    private String guideImage;

    @Schema(description = "是否开启（0不开启 1开启）")
    private Integer isEnabled = 0;

    @Schema(description = "浏览类型（0关闭 1开启）")
    private Integer browseType;


    @Schema(description = "浏览抽奖次数")
    private Integer browseCount;


    @Schema(description = "免费获得（0关闭 1开启）")
    private Integer freeStatus;


    @Schema(description = "免费限制次数")
    private Integer freeCount;


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
            this.dayNumberList = new ArrayList<>();
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
            this.timeRangeList = new ArrayList<>();
        }
    }


}

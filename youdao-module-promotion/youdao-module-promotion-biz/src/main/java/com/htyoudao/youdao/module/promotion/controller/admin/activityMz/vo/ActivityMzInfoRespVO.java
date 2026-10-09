package com.htyoudao.youdao.module.promotion.controller.admin.activityMz.vo;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.htyoudao.youdao.module.commodity.api.DTO.CommodityDTO;
import com.htyoudao.youdao.module.system.api.store.dto.StoreInfoDTO;
import com.htyoudao.youdao.module.system.api.store.dto.TagValueDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * 管理后台 - 满赠活动详情 Response VO
 */
@Schema(description = "管理后台 - 满赠活动详情 Response VO")
@Data
public class ActivityMzInfoRespVO {

    @Schema(description = "主键")
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    @Schema(description = "活动主表ID")
    private Long activityId;

    @Schema(description = "活动名称")
    private String activityName;

    @Schema(description = "优惠类型（1满N元赠商品，2满N件赠商品）")
    private Integer discountType;

    @Schema(description = "优惠规则（1阶梯优惠 2循环优惠）")
    private Integer discountRules;

    @Schema(description = "下单类型（1按品类限制，2按商品限制）")
    private Integer placeOrderType;

    @Schema(description = "参与品类（1全部，2仅单品，3仅套餐）")
    private Integer categoryType;

    @Schema(description = "下单商品（1全部商品，2指定商品）")
    private Integer placeOrderProduct;

    @Schema(description = "活动商品（1全部商品可用，2指定商品可用）")
    private Integer activityProduct;

    @Schema(description = "门店奖励库存（1共用库存，2独立库存）")
    private Integer giftInventoryType;

    @Schema(description = "用户参与限制（0不限制，1每人每天，2每人最多）")
    private Integer userLimitType;

    @Schema(description = "用户参与限制次数")
    private Integer userLimitValue;

    @Schema(description = "开始日期")
    private Date startDate;

    @Schema(description = "结束日期")
    private Date endDate;

    @Schema(description = "活动状态(1未开始 2进行中 3已结束)")
    private Integer activityStatus;

    @Schema(description = "应用范围 0 门店 1 标签")
    private Integer appScope;

    @Schema(description = "门店标签ID集合（应用范围=标签时）")
    private List<Long> tagIds;

    @Schema(description = "是否全门店 (1是 0否)")
    private Integer activityStore;

    @Schema(description = "适用门店")
    private List<StoreInfoDTO> storeInfoDTOS = new ArrayList<>();

    @Schema(description = "适用标签")
    private List<TagValueDTO> TagInfoDTOS = new ArrayList<>();

    @Schema(description = "适用商品")
    private List<CommodityDTO> commodityDTOList = new ArrayList<>();

    @Schema(description = "是否上架(0不开启 1开启)")
    private Integer isEnabled;

    @Schema(description = "是否可编辑")
    private Boolean isEditable = true;

    @Schema(description = "活动备注")
    private String activityRemark;

    @Schema(description = "优惠叠加（0不叠加 1叠加）")
    private Integer discountStackable;

    @Schema(description = "可叠加活动")
    private String stackableActivities;

    @Schema(description = "指定时间段")
    private String timeRange;

    @Schema(description = "指定日期逗号分割")
    private String dayNumbers;

    @Schema(description = "指定周几逗号分割")
    private String weekNumbers;

    @Schema(description = "可叠加活动标识列表")
    private List<Integer> stackableActivitieList;

    @Schema(description = "指定日期集合")
    private List<Integer> dayNumberList = new ArrayList<>();

    @Schema(description = "指定周几集合")
    private List<Integer> weekNumberList = new ArrayList<>();

    @Schema(description = "指定时间段集合")
    private List<String> timeRangeList = new ArrayList<>();

    @Schema(description = "赠送商品列表")
    private List<ActivityMzGiftRespVO> giftList = new ArrayList<>();

    // ========== 日期/周几/时间段 双向转换 ==========

    public void setDayNumberList(List<Integer> dayNumberList) {
        this.dayNumberList = dayNumberList;
        if (ObjectUtil.isNotEmpty(dayNumberList)) {
            this.dayNumbers = dayNumberList.toString().replace("[", "").replace("]", "").replace(" ", "");
        } else {
            this.dayNumbers = "";
        }
    }

    public void setWeekNumberList(List<Integer> weekNumberList) {
        this.weekNumberList = weekNumberList;
        if (ObjectUtil.isNotEmpty(weekNumberList)) {
            this.weekNumbers = weekNumberList.toString().replace("[", "").replace("]", "").replace(" ", "");
        } else {
            this.weekNumbers = "";
        }
    }

    public void setTimeRangeList(List<String> timeRangeList) {
        this.timeRangeList = timeRangeList;
        if (ObjectUtil.isNotEmpty(timeRangeList)) {
            this.timeRange = timeRangeList.toString().replace("[", "").replace("]", "").replace(" ", "");
        } else {
            this.timeRange = "";
        }
    }

    public void setDayNumbers(String dayNumbers) {
        this.dayNumbers = dayNumbers;
        if (ObjectUtil.isNotEmpty(dayNumbers)) {
            String[] array = dayNumbers.split(",");
            this.dayNumberList = new ArrayList<>();
            for (String s : array) {
                this.dayNumberList.add(Integer.parseInt(s));
            }
        } else {
            this.dayNumberList = null;
        }
    }

    public void setWeekNumbers(String weekNumbers) {
        this.weekNumbers = weekNumbers;
        if (ObjectUtil.isNotEmpty(weekNumbers)) {
            String[] array = weekNumbers.split(",");
            this.weekNumberList = new ArrayList<>();
            for (String s : array) {
                this.weekNumberList.add(Integer.parseInt(s));
            }
        } else {
            this.weekNumberList = null;
        }
    }

    public void setTimeRange(String timeRange) {
        this.timeRange = timeRange;
        if (ObjectUtil.isNotEmpty(timeRange)) {
            String[] array = timeRange.split(",");
            this.timeRangeList = new ArrayList<>();
            Collections.addAll(this.timeRangeList, array);
        } else {
            this.timeRangeList = null;
        }
    }

    /**
     * 赠送商品响应 VO
     */
    @Data
    public static class ActivityMzGiftRespVO {

        @Schema(description = "赠送商品记录id")
        private Long id;

        @Schema(description = "门店ID（门店独立库存时有值）")
        private Long storeId;

        @Schema(description = "优惠门槛")
        private BigDecimal threshold;

        @Schema(description = "赠送商品ID")
        private Long giftCommodityId;

        @Schema(description = "赠送商品名称")
        private String giftCommodityName;

        @Schema(description = "赠送商品价格")
        private BigDecimal giftPrice;

        @Schema(description = "赠品图片")
        private String giftImage;

        @Schema(description = "活动库存（null表示不限制，0表示无库存）")
        private Integer activityInventory;

        @Schema(description = "剩余库存")
        private Integer remainingInventory;
    }
}

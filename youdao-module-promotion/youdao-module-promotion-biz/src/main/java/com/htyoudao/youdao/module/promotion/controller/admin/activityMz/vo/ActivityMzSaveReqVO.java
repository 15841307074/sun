package com.htyoudao.youdao.module.promotion.controller.admin.activityMz.vo;

import cn.hutool.core.date.DateTime;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 管理后台 - 满赠活动 新增/修改 Request VO
 */
@Schema(description = "管理后台 - 满赠活动 新增/修改 Request VO")
@Data
public class ActivityMzSaveReqVO {

    @Schema(description = "id（修改时必传）")
    private Long id;

    /**
     * 营销活动ID（修改时必传，创建时不传）
     */
    private Long activityId;

    /**
     * 优惠类型（1满N元赠商品，2满N件赠商品）
     */
    @Schema(description = "优惠类型（1满N元赠商品，2满N件赠商品）")
    @NotNull(message = "优惠类型不能为空")
    private Integer discountType;

    /**
     * 优惠规则（1阶梯优惠 2循环优惠）
     */
    @Schema(description = "优惠规则（1阶梯优惠 2循环优惠）")
    @NotNull(message = "优惠规则不能为空")
    private Integer discountRules;

    /**
     * 活动名称
     */
    @Schema(description = "活动名称")
    @NotNull(message = "活动名称不能为空")
    private String activityName;

    /**
     * 下单类型（1按品类限制，2按商品限制）
     */
    @Schema(description = "下单类型（1按品类限制，2按商品限制）")
    private Integer placeOrderType;

    /**
     * 参与品类（1全部，2仅单品，3仅套餐；placeOrderType=1时生效）
     */
    @Schema(description = "参与品类（1全部，2仅单品，3仅套餐）")
    private Integer categoryType;

    /**
     * 下单商品（1全部商品，2指定商品；placeOrderType=2时生效）
     */
    @Schema(description = "下单商品（1全部商品，2指定商品）")
    private Integer placeOrderProduct;

    /**
     * 活动商品（1全部商品可用，2指定商品可用）
     */
    @Schema(description = "活动商品（1全部商品可用，2指定商品可用）")
    @NotNull(message = "活动商品不能为空")
    private Integer activityProduct;

    /**
     * 门店奖励库存（1共用库存，2独立库存）
     */
    @Schema(description = "门店奖励库存（1共用库存，2独立库存）")
    private Integer giftInventoryType;

    /**
     * 用户参与限制（0不限制，1每人每天，2每人最多）
     */
    @Schema(description = "用户参与限制（0不限制，1每人每天，2每人最多）")
    private Integer userLimitType;

    /**
     * 用户参与限制次数
     */
    @Schema(description = "用户参与限制次数")
    private Integer userLimitValue;

    /**
     * 优惠叠加（0不叠加，1叠加）
     */
    @Schema(description = "优惠叠加（0不叠加，1叠加）")
    @NotNull(message = "优惠叠加不能为空")
    private Integer discountStackable;

    /**
     * 可叠加活动（存储活动标识，如1-优惠券）
     */
    @Schema(description = "可叠加活动标识列表")
    private List<Integer> stackableActivitieList;

    /**
     * 活动备注
     */
    @Schema(description = "活动备注")
    private String activityRemark;

    @Schema(description = "应用范围 0 门店 1 标签")
    @NotNull(message = "应用范围不能为空")
    private Integer appScope;

    /**
     * 活动门店（1全部门店，2部分门店）
     */
    @Schema(description = "活动门店（1全部门店，2部分门店）")
    @NotNull(message = "活动门店不能为空")
    private Integer activityStore;

    /**
     * 开始日期
     */
    @Schema(description = "开始日期")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private DateTime startDate;

    /**
     * 结束日期
     */
    @Schema(description = "结束日期")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private DateTime endDate;

    @Schema(description = "指定日期集合（每月几号）")
    private List<Integer> dayNumberList;

    @Schema(description = "指定周几集合")
    private List<Integer> weekNumberList;

    @Schema(description = "指定时间段集合")
    private List<String> timeRangeList;

    @Schema(description = "赠送商品列表")
    private List<ActivityMzGiftReqVO> giftList;

    @Schema(description = "门店ID集合")
    private List<Long> storeIds;

    @Schema(description = "门店标签id集合")
    private List<Long> tagIds;

    @Schema(description = "商品ID集合（参与活动商品）")
    private List<Long> commodityIds;

    /**
     * 赠送商品 VO（内嵌）
     */
    @Data
    public static class ActivityMzGiftReqVO {

        @Schema(description = "赠送商品记录id（修改时传）")
        private Long id;

        @Schema(description = "优惠门槛（满赠金额/件数），小数点后两位，最大99999")
        @NotNull(message = "优惠门槛不能为空")
        private BigDecimal threshold;

        @Schema(description = "赠送商品ID")
        @NotNull(message = "赠送商品不能为空")
        private Long giftCommodityId;

        @Schema(description = "赠送商品名称")
        private String giftCommodityName;

        @Schema(description = "赠送商品价格")
        private BigDecimal giftPrice;

        @Schema(description = "赠品图片")
        private String giftImage;

        @Schema(description = "活动库存（不填表示不限制，0表示无库存，最大1000000）")
        private Integer activityInventory;
    }
}

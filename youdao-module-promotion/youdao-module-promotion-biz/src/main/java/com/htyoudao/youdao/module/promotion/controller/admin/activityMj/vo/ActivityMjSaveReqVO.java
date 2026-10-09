package com.htyoudao.youdao.module.promotion.controller.admin.activityMj.vo;


import cn.hutool.core.date.DateTime;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * @author Yangqinglin
 */
@Schema(description = "管理后台 - 营销活动 满减满折新增/修改 Request VO")
@Data
public class ActivityMjSaveReqVO {
    @Schema(description = "id")
    private Long id;

    /**
     * 营销活动ID
     */
    private Long activityId;

    /**
     * 优惠类型 （1满N元，2满N件）
     */
    @Schema(description = "优惠类型 （1满N元，2满N件）")
    @NotNull(message = "优惠类型 不能为空")
    private Integer discountType;

    /**
     * 活动名称
     */
    @Schema(description = "活动名称")
    @NotNull(message = "活动名称 不能为空")
    private String activityName;

    /**
     * 优惠折扣（1满xx元减xx元 2满xx元减xx折）
     */
    @Schema(description = "优惠折扣（1满xx元减xx元 2满xx元减xx折）")
    private Integer discountOffer;


    /**
     * 优惠规则（1阶梯优惠 2循环优惠）
     */
    @Schema(description = "优惠规则（1阶梯优惠 2循环优惠）")
    private Integer discountRules;

    /**
     * 优惠叠加（0不叠加 1叠加）
     */
    @Schema(description = "优惠叠加（0不叠加 1叠加）")
    @NotNull(message = "优惠叠加（0不叠加 1叠加） 不能为空")
    private Integer discountStackable;


    /**
     * 可叠加活动（存储活动标识，如1-优惠券）
     */
    @Schema(description = "存储活动标识，如1-优惠券")
    private List<Integer> stackableActivitieList;


    /**
     * 活动备注
     */
    @Schema(description = "活动备注")
    private String activityRemark;

    /**
     * 活动门店（1全部门店 2部分门店）
     */
    @Schema(description = "活动门店（1全部门店 2部分门店）")
    @NotNull(message = "活动门店 不能为空")
    private Integer activityStore;

    /**
     * 活动商品（1全部商品可用 2指定商品可用）
     */
    @Schema(description = "活动商品（1全部商品可用 2指定商品可用）")
    @NotNull(message = "活动商品 不能为空")
    private Integer activityProduct;

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

    @Schema(description = "日期集合")
    private List<Integer> dayNumberList;

    @Schema(description = "周几集合")
    private List<Integer> weekNumberList;

    @Schema(description = "时间段集合")
    private List<String> timeRangeList;

    @Schema(description = "设置集合")
    private List<String> settingList;

    @Schema(description = "应用范围 0 门店 1 标签")
    @NotNull(message = "应用范围不能为空")
    private Integer appScope;

    @Schema(description = "门店标签id集合")
    private List<Long> tagIds;

    @Schema(description = "门店ID集合")
    private List<Long> storeIds;

    @Schema(description = "商品ID集合")
    private List<Long> commodityIds;
}

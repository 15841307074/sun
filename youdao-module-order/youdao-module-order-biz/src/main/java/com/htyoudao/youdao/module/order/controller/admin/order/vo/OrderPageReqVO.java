package com.htyoudao.youdao.module.order.controller.admin.order.vo;

import co.elastic.clients.elasticsearch._types.FieldValue;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.htyoudao.youdao.framework.common.pojo.PageParam;
import com.htyoudao.youdao.framework.excel.core.pojo.SearchAfterSupport;
import com.htyoudao.youdao.module.order.util.DateUtils;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serial;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static com.htyoudao.youdao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY;
import static com.htyoudao.youdao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 订单分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
public class OrderPageReqVO extends PageParam implements SearchAfterSupport {
    @Serial
    private static final long serialVersionUID = 4787892849635444748L;

    @Schema(description = "是否查看历史订单", example = "0 进行中， 1 历史")
    private Integer orderStateHistory;

    @Schema(description = "订单号，模糊匹配", example = "youdao")
    private String orderSn;

    @Schema(description = "支付单号，模糊匹配", example = "youdao")
    private String paySn;

    @Schema(description = "订单状态：0-已取消；10-未付款订单；20-已付款；30-待取餐；40-代配送 50-已配送 60-已完成", example = "1")
    private Integer orderState;

    @Schema(description = "订单状态，多选：0-已取消；10-未付款订单；20-已付款；30-待取餐；40-代配送 50-已配送 60-已完成", example = "[80,50]")
    @JsonAlias("orderStates")
    private List<Integer> orderStateList;

    @Schema(description = "下单开始时间", example = "1")
    private LocalDateTime startOrderTime;

    @Schema(description = "下单结束时间", example = "1")
    private LocalDateTime endOrderTime;

    @Schema(description = "订单类型 0 堂食 1 打包 2 外卖 3 预订单", example = "1")
    private Integer orderType;

    @Schema(description = "订单类型，多选：0 堂食 1 打包 2 外卖 3代取", example = "[0,1]")
    @JsonAlias("orderTypes")
    private List<Integer> orderTypeList;

    @Schema(description = "用户优惠券ID", example = "1")
    private Long userCouponId;

    @Schema(description = "优惠券ID", example = "1")
    private Long couponId;

    @Schema(description = "优惠券名称", example = "xxxx")
    private String couponName;

    @Schema(description = "活动ID", example = "1")
    private Long activityId;

    @Schema(description = "活动名称", example = "xxxx")
    private String activityName;

    @Schema(description = "活动类型 1 n件n折", example = "1")
    private Integer activityType;

    @Schema(description = "支付方式code 0现金 1 微信 2支付宝", example = "1")
    private String paymentCode;

    @Schema(description = "订单渠道 1-微信小程序", example = "1")
    private Integer orderFrom;

    @Schema(description = "手机号 模糊", example = "1")
    private String mobile;

    @Schema(description = "是否是门店 0 否 1 是", example = "1")
    private Integer isStore;

    @Schema(description = "组织ID", example = "1")
    private Long orgId;

    private String channelType;

    private Long storeId;

    private String searchStr;

    private Set<Long> storeIds;

    private List<Long> memberId;

    @JsonIgnore
    private LocalDateTime[] createTime;

    private String startTime;

    private String endTime;

    private List<FieldValue> searchAfter;

    @Override
    public void setSearchAfter(List<FieldValue> searchAfter) {
        this.searchAfter = searchAfter;
    }

    @Override
    public List<FieldValue> getSearchAfter() {
        return searchAfter;
    }

    public void setEndOrderTime(String endOrderTime) {
        if (ObjectUtils.isEmpty(endOrderTime)) {
            this.endOrderTime = LocalDateTime.now();
        } else {
            this.endOrderTime = DateUtils.stringToLocalDateTime(endOrderTime);
        }
    }

    public void setStartOrderTime(String startOrderTime) {
        if (ObjectUtils.isEmpty(startOrderTime)) {
            this.startOrderTime = LocalDate.now().atStartOfDay();
        } else {
            this.startOrderTime = DateUtils.stringToLocalDateTime(startOrderTime);
        }
    }

    @JsonIgnore
    public List<Integer> getOrderStateQueryList() {
        return getIntegerQueryList(orderStateList, orderState);
    }

    @JsonIgnore
    public List<Integer> getOrderTypeQueryList() {
        return getIntegerQueryList(orderTypeList, orderType);
    }

    private List<Integer> getIntegerQueryList(List<Integer> values, Integer fallbackValue) {
        if (!ObjectUtils.isEmpty(values)) {
            return values.stream()
                    .filter(value -> !ObjectUtils.isEmpty(value))
                    .distinct()
                    .toList();
        }
        return ObjectUtils.isEmpty(fallbackValue) ? Collections.emptyList() : Collections.singletonList(fallbackValue);
    }
}

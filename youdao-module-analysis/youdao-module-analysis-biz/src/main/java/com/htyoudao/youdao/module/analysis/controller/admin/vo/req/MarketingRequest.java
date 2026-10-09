package com.htyoudao.youdao.module.analysis.controller.admin.vo.req;

import static com.htyoudao.youdao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.htyoudao.youdao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import lombok.Data;

import java.util.List;

/**
 * 管理后台 - 营销活动分析请求参数
 * 用于查询营销活动相关的分析数据，支持按优惠券、活动、渠道等维度筛选
 */
@Data
@Schema(description = "营销活动分析请求参数")
public class MarketingRequest extends PageParam {

    /**
     * 当前时间段-开始时间
     */
    @NotNull
    @Schema(description = "当前时间段-开始时间", example = "2024-01-01 00:00:00")
    @JsonFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime currentTimeStart;

    /**
     * 当前时间段-结束时间
     */
    @NotNull
    @Schema(description = "当前时间段-结束时间", example = "2024-01-31 23:59:59")
    @JsonFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime currentTimeEnd;

    /**
     * 门店ID列表
     * 用于筛选特定门店的数据
     */
    @Schema(description = "门店ID列表")
    private List<Long> storeIds;

    /**
     * 统计类型
     * all: 全部订单（含优惠）
     * coupon: 优惠券订单
     * activity: 营销活动订单
     */
    @Schema(description = "统计类型：all-全部, coupon-优惠券, activity-营销活动", example = "all")
    private String statType;

    /**
     * 渠道ID列表
     * 用于按渠道筛选数据，null表示全部渠道
     */
    @Schema(description = "渠道ID列表，null表示全部渠道")
    private List<Long> channelIds;

    /**
     * 优惠券ID列表
     * 用于筛选特定优惠券的数据，空列表表示全部优惠券
     */
    @Schema(description = "优惠券ID列表，空列表表示全部优惠券")
    private List<Long> couponIds;

    /**
     * 营销活动ID列表
     * 用于筛选特定营销活动的数据，空列表表示全部活动
     */
    @Schema(description = "营销活动ID列表，空列表表示全部活动")
    private List<Long> activityIds;

    /**
     * 是否显示UV数据
     * true: 返回UV（独立访客数）数据
     * false: 不返回UV数据
     */
    @Schema(description = "是否显示UV数据", example = "true")
    private Boolean showUV = true;

    /**
     * 排序字段
     * 指定按哪个字段进行排序，如销售金额、订单数量等
     */
    @Schema(description = "排序字段", example = "salesAmount")
    private String sortBy = "";

    /**
     * 排序方式
     * asc: 升序
     * desc: 降序
     */
    @Schema(description = "排序方式 asc 或 desc", example = "desc")
    private String sortOrder;

}

package com.htyoudao.youdao.module.analysis.service.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.htyoudao.youdao.module.analysis.enums.EventType;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Data;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import static com.htyoudao.youdao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

/**
 * 事件查询数据传输对象
 * 用于封装事件（UV/PV等）查询的筛选条件
 */
@Data
public class EventQueryDTO {

    /**
     * 门店ID列表
     * 用于筛选特定门店的事件数据
     */
    private List<Long> storeIds;

    /**
     * 事件类型
     * 如到店事件、下单事件等
     */
    private EventType eventType;

    /**
     * 事件类型列表
     * 用于一次性查询多种类型的事件
     */
    private List<EventType> eventTypes;

    /**
     * 开始时间
     * 统计时间范围的起始时间
     */
    @Schema(description = "开始时间", example = "2024-01-01 00:00:00")
    @JsonFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime startTime;

    /**
     * 结束时间
     * 统计时间范围的结束时间
     */
    @Schema(description = "结束时间", example = "2024-01-31 23:59:59")
    @JsonFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime endTime;

    /**
     * 事件ID
     * 用于精确查询特定事件的数据，如优惠券ID、商品ID等
     */
    @Schema(description = "事件ID，如优惠券ID、商品ID等")
    private String eventId;

    /**
     * 事件ID列表
     * 用于批量查询多个事件的数据
     */
    @Schema(description = "事件ID列表，如优惠券ID列表、商品ID列表")
    private List<String> eventIds;

    /**
     * 是否新客
     * true: 只统计新客事件
     * false/null: 统计全部
     */
    @Schema(description = "是否新客事件")
    private Boolean isNew;

    /**
     * 渠道ID列表
     * 用于按渠道筛选事件数据
     */
    private List<Long> channelIds;

    /**
     * 是否精准查询pv，为false时最大值为10000
     */
    private Boolean trackTotalHits = false;
}



package com.htyoudao.youdao.module.analysis.service.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Data;

import static com.htyoudao.youdao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

/**
 * 广告事件查询数据传输对象
 * 用于封装广告埋点（曝光/点击/离开）统计查询的筛选条件
 */
@Data
public class AdEventQueryDTO {

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
     * 广告事件类型
     * ad_exposure:曝光 ad_click:点击 ad_leave:离开
     */
    @Schema(description = "广告事件类型编码")
    private String eventType;

    /**
     * 广告ID列表
     * 用于批量筛选多个广告的数据
     */
    @Schema(description = "广告ID列表")
    private List<Long> adIds;

    /**
     * 广告位序号列表
     * 用于按广告位筛选数据，取值范围1-12
     */
    @Schema(description = "广告位序号列表")
    private List<Integer> adInfoPositions;

    /**
     * 门店ID列表
     * 用于筛选特定门店的广告事件数据
     */
    private List<Long> storeIds;

    /**
     * 渠道ID列表
     * 用于按渠道筛选广告事件数据
     */
    private List<String> channelIds;

    /**
     * 用户ID
     * 用于查询单个用户的广告事件数据
     */
    @Schema(description = "用户ID")
    private Long memberId;

}

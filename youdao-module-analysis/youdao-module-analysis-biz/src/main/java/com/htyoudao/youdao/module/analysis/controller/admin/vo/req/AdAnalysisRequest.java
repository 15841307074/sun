package com.htyoudao.youdao.module.analysis.controller.admin.vo.req;

import static com.htyoudao.youdao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Data;

/**
 * 管理后台 - 广告分析请求参数
 * 用于查询广告埋点相关的分析数据，支持按广告、广告位、门店、渠道等维度筛选
 */
@Data
@Schema(description = "广告分析请求参数")
public class AdAnalysisRequest {

    /**
     * 开始时间
     */
    @NotNull
    @Schema(description = "开始时间", example = "2024-01-01 00:00:00")
    @JsonFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime startTime;

    /**
     * 结束时间
     */
    @NotNull
    @Schema(description = "结束时间", example = "2024-01-31 23:59:59")
    @JsonFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime endTime;

    /**
     * 广告ID列表
     * 用于筛选特定广告的数据，空表示全部广告
     */
    @Schema(description = "广告ID列表，空表示全部广告")
    private List<Long> adIds;

    /**
     * 广告位序号列表
     * 用于按广告位筛选数据，取值范围1-14，空表示全部广告位
     */
    @Schema(description = "广告位序号列表，空表示全部广告位")
    private List<Integer> adInfoPositions;

    /**
     * 门店ID列表
     * 用于筛选特定门店的数据
     */
    @Schema(description = "门店ID列表")
    private List<Long> storeIds;

    /**
     * 渠道ID列表
     * 用于按渠道筛选数据，null表示全部渠道
     */
    @Schema(description = "渠道ID列表，null表示全部渠道")
    private List<String> channelIds;

}

package com.htyoudao.youdao.module.analysis.controller.admin.vo.req;

import static com.htyoudao.youdao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.htyoudao.youdao.module.analysis.service.dto.EsAggDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 管理后台 - 数据分析聚合请求参数
 * 用于查询指定时间范围内的数据聚合指标，支持同比分析
 */
@Schema(description = "管理后台 - 聚合请求参数对象 Request VO")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AggregationRequestVO  {
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
     * 同比时间段-开始时间
     * 用于与当前时间段进行对比分析
     */
    @NotNull
    @Schema(description = "同比时间段-开始时间", example = "2023-01-01 00:00:00")
    @JsonFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime beforeTimeStart;

    /**
     * 同比时间段-结束时间
     * 用于与当前时间段进行对比分析
     */
    @NotNull
    @Schema(description = "同比时间段-结束时间", example = "2023-01-31 23:59:59")
    @JsonFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime beforeTimeEnd;

    /**
     * 订单来源
     * 0:点餐 1:微信 2:支付宝
     * 不传表示全部来源
     */
    @Schema(description = "订单来源 0点餐 1微信 2支付宝，不传表示全部", example = "1")
    private Integer orderFrom;

    /**
     * 门店ID列表
     * 用于筛选特定门店的数据，不传表示全部门店
     */
    @Schema(description = "门店ID列表，不传表示全部门店")
    private List<Long> storeIds;

    /**
     * 用户类型
     * 1: 新客 2: 老客 3: 会员
     * 不传表示全部用户类型
     */
    @Schema(description = "用户类型 1: 新客 2: 老客 3: 会员 不传: 全部", example = "1")
    private Integer userType;


    /**
     * 构建当前时间段的查询参数
     * 将请求参数转换为EsAggDTO用于ES查询
     *
     * @return 当前时间段的ES聚合查询参数
     */
    public EsAggDTO buildCurrentRequest(){
        EsAggDTO esAggDTO = new EsAggDTO();
        esAggDTO.setTimes(new LocalDateTime[]{currentTimeStart,currentTimeEnd});
        esAggDTO.setStoreIds(storeIds);
        if (orderFrom != null && orderFrom != -1){
            esAggDTO.setOrderFroms(Collections.singletonList(orderFrom));
        }

        if (userType != null) {
            switch (userType){
                case 1:esAggDTO.setExpressId(1);break;
                case 2:esAggDTO.setExpressId(0);break;
                case 3:esAggDTO.setIsSettlement(1);break;
            }
        }
        return esAggDTO;
    }

    /**
     * 构建同比时间段的查询参数
     * 将请求参数转换为EsAggDTO用于ES查询
     *
     * @return 同比时间段的ES聚合查询参数
     */
    public EsAggDTO buildBeforeRequest(){
        EsAggDTO esAggDTO = new EsAggDTO();
        esAggDTO.setTimes(new LocalDateTime[]{beforeTimeStart,beforeTimeEnd});
        esAggDTO.setStoreIds(storeIds);
        if (orderFrom != null && orderFrom != -1){
            esAggDTO.setOrderFroms(Collections.singletonList(orderFrom));
        }

        if (userType != null) {
            switch (userType){
                case 1:esAggDTO.setExpressId(1);break;
                case 2:esAggDTO.setExpressId(0);break;
                case 3:esAggDTO.setIsSettlement(1);break;
            }
        }
        return esAggDTO;
    }
}
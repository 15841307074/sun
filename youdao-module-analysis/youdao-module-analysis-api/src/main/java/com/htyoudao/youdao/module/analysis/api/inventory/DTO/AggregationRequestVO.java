package com.htyoudao.youdao.module.analysis.api.inventory.DTO;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static com.htyoudao.youdao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 聚合请求参数对象 Request VO")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AggregationRequestVO {
    @NotNull
    @Schema(description = "当前时间段")
    @JsonFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    @JsonSerialize(using = LocalDateTimeSerializer.class) // 序列化
    @JsonDeserialize(using = LocalDateTimeDeserializer.class) // 反序列化
    private LocalDateTime currentTimeStart;

    @NotNull
    @Schema(description = "当前时间段")
    @JsonFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    @JsonSerialize(using = LocalDateTimeSerializer.class) // 序列化
    @JsonDeserialize(using = LocalDateTimeDeserializer.class) // 反序列化
    private LocalDateTime currentTimeEnd;

    @NotNull
    @Schema(description = "同比时间段")
    @JsonFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    @JsonSerialize(using = LocalDateTimeSerializer.class) // 序列化
    @JsonDeserialize(using = LocalDateTimeDeserializer.class) // 反序列化
    private LocalDateTime beforeTimeStart;

    @NotNull
    @Schema(description = "同比时间段")
    @JsonFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    @JsonSerialize(using = LocalDateTimeSerializer.class) // 序列化
    @JsonDeserialize(using = LocalDateTimeDeserializer.class) // 反序列化
    private LocalDateTime beforeTimeEnd;

    @Schema(description = "订单来源 0点餐 1微信 2支付宝")
    private Integer orderFrom;

    @Schema(description = "门店 id")
    private List<Long> storeIds;

    @Schema(description = "用户类型 1: 新客 2: 老客 3: 会员 不传: 全部")
    private Integer userType;


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
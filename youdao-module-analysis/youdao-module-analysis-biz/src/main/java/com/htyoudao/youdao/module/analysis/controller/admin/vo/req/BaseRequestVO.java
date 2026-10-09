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
import lombok.Data;

@Data
public class BaseRequestVO {
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


    @Schema(description = "门店 id")
    private List<Long> storeIds;

    public EsAggDTO buildCurrentRequest(){
        EsAggDTO esAggDTO = new EsAggDTO();
        esAggDTO.setTimes(new LocalDateTime[]{currentTimeStart,currentTimeEnd});
        esAggDTO.setStoreIds(storeIds);
        return esAggDTO;
    }


    public EsAggDTO buildBeforeRequest(){
        EsAggDTO esAggDTO = new EsAggDTO();
        esAggDTO.setTimes(new LocalDateTime[]{beforeTimeStart,beforeTimeEnd});
        esAggDTO.setStoreIds(storeIds);
        return esAggDTO;
    }

}

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
import java.util.List;
import java.util.Map;
import lombok.Data;

@Data
public class ProductSingleRequest extends PageParam {

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

    public List<Long> storeIds;

    @Schema(description = "商品ID")
    private String commodityId;

    @Schema(description = "分类名称")
    private String categoryName;

    @Schema(description = "单品1 套餐0")
    private Integer isSingle;


}

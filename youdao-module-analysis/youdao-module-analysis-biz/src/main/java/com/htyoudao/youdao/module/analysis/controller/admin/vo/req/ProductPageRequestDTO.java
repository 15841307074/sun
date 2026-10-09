package com.htyoudao.youdao.module.analysis.controller.admin.vo.req;

import static com.htyoudao.youdao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.htyoudao.youdao.framework.common.pojo.PageParam;
import com.htyoudao.youdao.module.analysis.service.dto.EsAggDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import lombok.Data;

@Data
public class ProductPageRequestDTO extends PageParam {

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

    @Schema(description = "排序字段")
    public String sortBy;

    @Schema(description = "排序方式 asc desc")
    public String sortOrder;

    @Schema(description = "门店ID集合")
    public List<Long> storeIds;

    @Schema(description = "商品名称")
    private String goodsName;

    @Schema(description = "单品1 套餐2 加购3")
    private Integer isSingle;

    @Schema(description = "是否为实时 true时增加在售门店数返回")
    private Boolean realTime = false;

    @Schema(description = "商品原始ID")
    private List<Long> commodityIds;

    @Schema(description = "商品指标列表")
    private Set<String> metrics;


    public EsAggDTO buildRequest(){
        EsAggDTO esAggDTO = new EsAggDTO();
        esAggDTO.setTimes(new LocalDateTime[]{currentTimeStart,currentTimeEnd});
        esAggDTO.setStoreIds(storeIds);
        return esAggDTO;
    }

}
